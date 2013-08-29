package org.codeandmagic.findmefood.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.codeandmagic.findmefood.QTile;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.ZoomLevel;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.service.HttpGetPlaces;
import org.codeandmagic.findmefood.service.HttpResponseParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.LocationUtils.MIN_DATA_FETCH_ZOOM;
import static org.codeandmagic.findmefood.LocationUtils.INITIAL_DISPLAY_ZOOM;
import static org.codeandmagic.findmefood.MyApplication.LOCATION_CACHE;
import static org.codeandmagic.findmefood.Utils.QTILE_ARR_EMPTY;
import static org.codeandmagic.findmefood.Utils.diff;

// TODO If Google Places API gives back the same place for two different QTiles it must be drawn only once
public class PlacesMapFragmentWithCache extends Fragment implements OnCameraChangeListener {

    private GoogleMap googleMap;
    private BitmapDescriptor iconPin;
    private RequestQueue requestQueue;
    private HttpResponseParser responseParser;

    private Map<QTile, Polygon> drawnTiles = new HashMap<QTile, Polygon>();
    private Map<QTile, List<Marker>> markers = new HashMap<QTile, List<Marker>>();
    private QTile[] visibleTiles = QTILE_ARR_EMPTY;

    private Toast toast;

    private int currentTileColor;
    private int neighbourTileColor;
    private int tileCenterColor;

    public static PlacesMapFragmentWithCache newInstance(Bundle args) {
        PlacesMapFragmentWithCache fragment = new PlacesMapFragmentWithCache();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        currentTileColor = getResources().getColor(R.color.current_tile);
        neighbourTileColor = getResources().getColor(R.color.neighbour_tile);
        tileCenterColor = getResources().getColor(R.color.tile_center);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.places_map_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity());
        responseParser = new HttpResponseParser();
        initBitmapMarkers();
        setupMapFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGoogleMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(false);
        }
    }

    private void initBitmapMarkers() {
        try {
            // Must call initialize() before getting the BitmapDescriptorFactory.
            MapsInitializer.initialize(getActivity());
            iconPin = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_blue);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.w(APP_TAG, "GooglePlayServices not available.");
        }
    }

    private void setupMapFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if (mapFragment == null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().zoom(INITIAL_DISPLAY_ZOOM).target(new LatLng(51.5112, -0.119824)).build();
            mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().camera(cameraPosition));
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
    }

    private void setupGoogleMap() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

            if (googleMap == null) {
                Toast.makeText(getActivity(), R.string.google_maps_not_supported, Toast.LENGTH_LONG).show();
                return;
            }
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnCameraChangeListener(this);
        googleMap.setMyLocationEnabled(true);
    }

    private ZoomLevel getDataFetchZoom(float cameraZoom) {
        int seeAllScreenZoom = (int) (cameraZoom - 1);
        if(seeAllScreenZoom < 1) {
            return ZoomLevel.Z1;
        }
        if(seeAllScreenZoom > MIN_DATA_FETCH_ZOOM) {
            return ZoomLevel.get(MIN_DATA_FETCH_ZOOM);
        }
        return  ZoomLevel.get(seeAllScreenZoom);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        final LatLng latLng = cameraPosition.target;
        final ZoomLevel zoom = getDataFetchZoom(cameraPosition.zoom);
        final QTile centerTile = QTile.forCenter(latLng, zoom);
        centerTile.loadNeighbours();

        //Log.v(APP_TAG, centerTile.toString());

        QTile[] newTiles = new QTile[4];
        newTiles[0] = centerTile;

        QTile[] neighbours = centerTile.closestNeighbours(latLng);
        System.arraycopy(neighbours, 0, newTiles, 1, neighbours.length);

        QTile[] removedTiles = diff(visibleTiles, newTiles, QTILE_ARR_EMPTY);
        QTile[] addedTiles = diff(newTiles, visibleTiles, QTILE_ARR_EMPTY);

        //Log.d(APP_TAG, "Visible tiles: " + visibleTiles.length);
        //Log.d(APP_TAG, "New tiles: " + newTiles.length);
        //Log.d(APP_TAG, "Removed tiles: " + removedTiles.length);
        //Log.d(APP_TAG, "Added tiles: " + addedTiles.length);

        // Delete markers for all remove drawnTiles
        for (QTile tile : removedTiles) {
            removePlacesForQTile(tile);
        }

        // Delete all tile debug squares
        for(Map.Entry<QTile, Polygon> entry : drawnTiles.entrySet()) {
           entry.getValue().remove();
        }
        drawnTiles.clear();

        // Add all new tile debug squares
        for(QTile tile : newTiles) {
            drawnTiles.put(tile, drawQTile(tile, tile.equals(centerTile) ? currentTileColor : neighbourTileColor));
        }

        // Add markers for new added drawnTiles:
        // First verify if the drawnTiles are in cache.
        // If not, request places for the new drawnTiles.
        for (QTile qTile : addedTiles) {
            Collection<Place> places = LOCATION_CACHE.get(qTile);
            if (places != null) {
                addPlacesForQTile(qTile, places);
            } else {
                requestPlaces(qTile);
            }
        }

        visibleTiles = newTiles;
    }

    private void removePlacesForQTile(QTile qTile) {
        List<Marker> list = markers.get(qTile);
        if (list != null) {
            for (Marker marker : list) {
                marker.remove();
            }
        }
        markers.remove(qTile);
    }

    private void addPlacesForQTile(QTile qTile, Collection<Place> places) {
        List<Marker> list = new ArrayList<Marker>();
        for (Place place : places) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(iconPin)
                    .position(place.getLatLng())
                    .title(place.getName())
                    .snippet(place.getVicinity());

            list.add(googleMap.addMarker(markerOptions));
        }
        markers.put(qTile, list);
    }


    private void requestPlaces(final QTile qTile) {
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getActivity(), "Get places for QTile " + qTile.quadKey, Toast.LENGTH_SHORT);
        toast.show();

        // TODO If Google API get the radius of the inner circle of one tile because Google might give places outside that QTile
        final HttpGetPlaces request = new HttpGetPlaces()
                .setLocation(qTile.center.latitude, qTile.center.longitude)
                .setRadius(qTile.radius);

        final String url = request.buildUrl(getActivity());
        Log.i(APP_TAG, "Url: " + url);

        requestQueue.add(new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final HttpResponseParser.PlaceParseResponse parseResponse = responseParser.parsePlaces(response);

                LOCATION_CACHE.add(qTile, parseResponse.places);
                Collection<Place> places = LOCATION_CACHE.get(qTile);
                addPlacesForQTile(qTile, places);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        ));
    }

    private Polygon drawQTile(QTile tile, int color) {
        //drawCenter(tile, tileCenterColor);

        return googleMap.addPolygon(new PolygonOptions()
                .add(tile.topLeft, tile.topRight, tile.bottomRight, tile.bottomLeft)
                .strokeColor(Color.BLUE)
                .strokeWidth(2)
                .fillColor(color));
    }

    private Circle drawCenter(QTile tile, int color) {
        return googleMap.addCircle(new CircleOptions()
                .center(tile.center)
                .radius(tile.radius)
                .strokeColor(Color.BLACK)
                .strokeWidth(3)
                .fillColor(color));
    }

}
