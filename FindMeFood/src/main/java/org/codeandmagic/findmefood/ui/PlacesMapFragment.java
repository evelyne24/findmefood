package org.codeandmagic.findmefood.ui;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.maps.GoogleMap.*;
import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Intents.EXTRA_PLACE;
import static org.codeandmagic.findmefood.Consts.Loaders.LOAD_PLACES;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.MY_LOCATION;
import static org.codeandmagic.findmefood.provider.PlacesDatabase.Places;

/**
 * Created by evelyne24.
 */
public class PlacesMapFragment extends Fragment implements OnMyLocationChangeListener, OnInfoWindowClickListener,
        OnCameraChangeListener, LoaderCallbacks<Cursor> {

    private static final int MAP_ZOOM = 13;
    private GoogleMap googleMap;
    private LatLng myLocation;
    private BitmapDescriptor iconPin;
    private BitmapDescriptor iconCurrentPin;

    private Map<Place, Marker> placeMarkers = new HashMap<Place, Marker>();
    private Place currentPlace;

    public static PlacesMapFragment newInstance(Place place) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PLACE, place);
        PlacesMapFragment fragment = new PlacesMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.places_map_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreInstanceState(savedInstanceState);
        initBitmapMarkers();
        setupMapFragment();
    }

    private void initBitmapMarkers() {
        try {
            // Must call initialize() before getting the BitmapDescriptorFactory.
            MapsInitializer.initialize(getActivity());
            iconPin = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_blue);
            iconCurrentPin = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.w(APP_TAG, "GooglePlayServices not available.");
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            myLocation = savedInstanceState.getParcelable(MY_LOCATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGoogleMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        setMyLocationUpdatesEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MY_LOCATION, myLocation);
        super.onSaveInstanceState(outState);
    }

    private void setupMapFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if (mapFragment == null) {
            fragmentManager.beginTransaction().replace(R.id.map, SupportMapFragment.newInstance()).commit();
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
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnCameraChangeListener(this);
        setMyLocationUpdatesEnabled(true);

        // initLoader gets triggered before the GoogleMap is ready after a rotation!
        getLoaderManager().restartLoader(LOAD_PLACES, null, this);
    }

    private void setMyLocationUpdatesEnabled(boolean enabled) {
        googleMap.setMyLocationEnabled(enabled);
        googleMap.setOnMyLocationChangeListener(enabled ? this : null);
    }

    @Override
    public void onMyLocationChange(Location location) {
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (googleMap.getMyLocation() == null) {
            centerMapOnLocation(myLocation);
        }
    }

    private void centerMapOnLocation(LatLng location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        myLocation = cameraPosition.target;

        if (myLocation.latitude != 0 && myLocation.longitude != 0) {

        }
    }

    private double computeVisibleRadius(LatLngBounds latLngBounds) {
        double startLat, startLng, endLat, endLng;

        if (latLngBounds.northeast.latitude < latLngBounds.southwest.latitude) {
            startLat = latLngBounds.northeast.latitude;
            endLat = latLngBounds.southwest.latitude;
        } else {
            endLat = latLngBounds.northeast.latitude;
            startLat = latLngBounds.southwest.latitude;
        }

        if (latLngBounds.northeast.longitude < latLngBounds.southwest.longitude) {
            startLng = latLngBounds.northeast.longitude;
            endLng = latLngBounds.southwest.longitude;
        } else {
            endLng = latLngBounds.northeast.longitude;
            startLng = latLngBounds.southwest.longitude;
        }

        return Math.sqrt(Math.pow((endLat - startLat) / 2, 2) + Math.pow((endLng - startLng) / 2, 2)) * (Math.PI / 180);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Places.CONTENT_URI, Places.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (LOAD_PLACES == loader.getId()) {
            List<Place> places = PlacesDatabase.readPlaces(cursor);
            placeMarkers.clear();

            for (Place place : places) {
                placeMarkers.put(place, getMarker(place));
            }

            // If we have started the fragment with a Place, make sure to update it as the current one
            Bundle args = getArguments();
            if (googleMap != null && args != null && args.containsKey(EXTRA_PLACE)) {
                Place place = args.getParcelable(EXTRA_PLACE);
                refreshCurrentPlace(place);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (LOAD_PLACES == loader.getId()) {
            clearAllMarkers();
        }
    }

    private Marker createMarker(Place place, BitmapDescriptor pinIcon) {
        LatLng position = new LatLng(place.getGeometry().getLocation().getLatitude(),
                place.getGeometry().getLocation().getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(place.getName())
                .snippet(place.getVicinity())
                .icon(pinIcon);

        return googleMap.addMarker(markerOptions);
    }

    private Marker getMarker(Place place) {
        return createMarker(place, iconPin);
    }

    private Marker getCurrentMarker(Place place) {
        return createMarker(place, iconCurrentPin);
    }

    private void clearAllMarkers() {
        placeMarkers.clear();
        if (googleMap != null) {
            googleMap.clear();
        }
    }


    public void refreshCurrentPlace(Place place) {
        // Swap the previous current marker from red to blue
        if(currentPlace != null) {
            Marker prevCurrentMarker = placeMarkers.get(currentPlace);
            prevCurrentMarker.remove();
            placeMarkers.put(currentPlace, getMarker(currentPlace));
        }

        // Swap the new current marker from blue to red
        Marker marker = placeMarkers.get(place);
        if(marker != null) {
            marker.remove();
        }
        currentPlace = place;
        Marker currentMarker = getCurrentMarker(currentPlace);
        placeMarkers.put(currentPlace, currentMarker);

        // Center map on new current marker
        centerMapOnLocation(currentMarker.getPosition());
        currentMarker.showInfoWindow();
    }
}
