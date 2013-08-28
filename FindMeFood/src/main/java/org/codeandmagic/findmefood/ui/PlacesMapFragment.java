package org.codeandmagic.findmefood.ui;

import android.database.Cursor;
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
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Intents.EXTRA_PLACE;
import static org.codeandmagic.findmefood.Consts.Loaders.LOAD_PLACES;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.CURRENT_PLACE;
import static org.codeandmagic.findmefood.provider.PlacesDatabase.Places;

/**
 * Created by evelyne24.
 */
public class PlacesMapFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final LatLng INITIAL_LAT_LNG = new LatLng(51.5112, -0.119824);
    private static final int INITIAL_ZOOM_LEVEL = 13;


    private GoogleMap googleMap;
    private BitmapDescriptor iconPin;
    private BitmapDescriptor iconCurrentPin;
    private Place currentPlace;

    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public static PlacesMapFragment newInstance(Bundle args) {
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

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentPlace = savedInstanceState.getParcelable(CURRENT_PLACE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CURRENT_PLACE, currentPlace);
        super.onSaveInstanceState(outState);
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

    private void setupMapFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if (mapFragment == null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(INITIAL_LAT_LNG)
                    .zoom(INITIAL_ZOOM_LEVEL).build();
            GoogleMapOptions options = new GoogleMapOptions().camera(cameraPosition);
            mapFragment = SupportMapFragment.newInstance(options);
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
        googleMap.setMyLocationEnabled(true);

        // initLoader gets triggered before the GoogleMap is ready after a rotation!
        getLoaderManager().restartLoader(LOAD_PLACES, null, this);
    }

    private void centerMapOnLocation(LatLng location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Places.CONTENT_URI, Places.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (LOAD_PLACES == loader.getId()) {
            List<Place> places = PlacesDatabase.readPlaces(cursor);
            markers.clear();

            for (Place place : places) {
                markers.put(place.getId(), getMarker(place));
            }

            // If we have started the fragment with a Place, make sure to update it as the current one
            Bundle args = getArguments();
            if (googleMap != null && args != null && args.containsKey(EXTRA_PLACE)) {
                Place place = args.getParcelable(EXTRA_PLACE);
                refreshCurrentPlace(place);
            } else if (currentPlace != null) {
                refreshCurrentPlace(currentPlace);
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
        markers.clear();
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    public void refreshCurrentPlace(Place place) {
        // Swap the previous current marker from red to blue
        if (currentPlace != null) {
            Marker prevCurrentMarker = markers.get(currentPlace);
            if(prevCurrentMarker != null) {
                prevCurrentMarker.remove();
            }
            markers.put(currentPlace.getId(), getMarker(currentPlace));
        }

        // Swap the new current marker from blue to red
        Marker marker = markers.get(place.getId());
        if (marker != null) {
            marker.remove();
        }
        currentPlace = place;
        Marker currentMarker = getCurrentMarker(currentPlace);
        markers.put(currentPlace.getId(), currentMarker);

        // Center map on new current marker
        centerMapOnLocation(currentMarker.getPosition());
        currentMarker.showInfoWindow();
    }

}
