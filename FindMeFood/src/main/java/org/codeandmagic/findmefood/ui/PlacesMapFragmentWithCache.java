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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.codeandmagic.findmefood.MyApplication;
import org.codeandmagic.findmefood.QTile;
import org.codeandmagic.findmefood.QuadKey;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.ZoomLevel;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.model.PlaceLocation;
import org.codeandmagic.findmefood.service.HttpGetPlaces;
import org.codeandmagic.findmefood.service.HttpResponseParser;

import java.text.MessageFormat;
import java.util.ArrayList;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.LocationUtils.CLUSTERING_ZOOM_LEVEL;
import static org.codeandmagic.findmefood.LocationUtils.DATA_FETCH_ZOOM_LEVEL;
import static org.codeandmagic.findmefood.LocationUtils.STARTING_ZOOM_LEVEL;

import static org.codeandmagic.findmefood.Utils.*;

/**
 * Created by evelyne24.
 */
public class PlacesMapFragmentWithCache extends Fragment implements OnCameraChangeListener {

    private GoogleMap googleMap;
    private BitmapDescriptor iconPin;
    private RequestQueue requestQueue;
    private HttpResponseParser responseParser;

    private QTile[] visibleTiles = new QTile[4];

    public static PlacesMapFragmentWithCache newInstance(Bundle args) {
        PlacesMapFragmentWithCache fragment = new PlacesMapFragmentWithCache();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
            CameraPosition cameraPosition = new CameraPosition.Builder().zoom(STARTING_ZOOM_LEVEL).target(new LatLng(51.5112, -0.119824)).build();
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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        final LatLng latLng = cameraPosition.target;
        final ZoomLevel zoom = ZoomLevel.get(DATA_FETCH_ZOOM_LEVEL);
        final QTile tile = QTile.forCenter(latLng, zoom);
        tile.loadNeighbours();

        googleMap.clear();

        drawQTile(tile, Color.parseColor("#33ff0000"));

        Log.v(APP_TAG, tile.toString());

        QTile[] newTiles = new QTile[4];
        newTiles[0] = tile;

        QTile[] neighbours = tile.closestNeighbours(latLng);
        System.arraycopy(neighbours, 0, newTiles, 1, neighbours.length);

        for (QTile neighbour : neighbours) {
            //Log.v(APP_TAG, "---->" + neighbour.toString());
            drawQTile(neighbour, Color.parseColor("#330000ff"));

        }

        QTile[] removedTiles = diff(visibleTiles, newTiles);
        QTile[] addedTiles = diff(newTiles, visibleTiles);

        // Delete markers for all remove tiles

        // Add markers for new added tiles:
        // First verify if the tiles are in cache.
        // If not, request places for the new tiles.
    }

    private void drawQTile(QTile tile, int color) {
        googleMap.addPolygon(new PolygonOptions()
                .add(tile.topLeft, tile.topRight, tile.bottomRight, tile.bottomLeft)
                .strokeColor(Color.BLUE)
                .strokeWidth(2)
                .fillColor(color));
    }

    private void requestPlaces(final QTile qTile, final LatLng centerLatLng) {
        final HttpGetPlaces request = new HttpGetPlaces().setLocation(centerLatLng.latitude, centerLatLng.longitude).setRadius(qTile.radius);
        final ZoomLevel clusteringZoomLevel = ZoomLevel.get(CLUSTERING_ZOOM_LEVEL);

        requestQueue.add(new StringRequest(Request.Method.GET, request.getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final HttpResponseParser.PlaceParseResponse parseResponse = responseParser.parsePlaces(response);
                final int size = parseResponse.places.size();
                final ArrayList<Marker> markers = new ArrayList<Marker>();


                for (int i = 0; i < size; ++i) {
                    Place place = parseResponse.places.get(i);
                    PlaceLocation location = place.getGeometry().getLocation();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    String quadKey = QuadKey.getQuadKey(latLng, clusteringZoomLevel);

                    // Check if this place is in the cache
                    if(quadKey.startsWith(qTile.quadKey)) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .icon(iconPin)
                                .position(latLng)
                                .title(place.getName())
                                .snippet(place.getVicinity());

                        markers.add(googleMap.addMarker(markerOptions));
                    }
                }

                Log.v(APP_TAG, MessageFormat.format("Fetched {0} places and added {1} to cache.", size, markers.size()));
                MyApplication.LOCATION_CACHE.add(qTile, markers.toArray(new Marker[markers.size()]));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        ));
    }


}
