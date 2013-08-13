package org.codeandmagic.findmefood.ui;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.model.PlaceLocation;

import java.util.List;

import static org.codeandmagic.findmefood.Consts.SavedInstanceState.*;

/**
 * Created by evelyne24.
 */
public class PlacesMapFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnCameraChangeListener {

    private static final int MAP_ZOOM = 13;
    private GoogleMap googleMap;
    private LatLng myLocation;
    private Marker myLocationMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.places_map_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreInstanceState(savedInstanceState);
        setupMapFragment();
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
        }

        if (googleMap != null) {
            setMyLocationUpdatesEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setOnInfoWindowClickListener(this);
            googleMap.setOnCameraChangeListener(this);

            if (myLocation != null) {
                // Immediately show my location if available
                showMyLocationMarker(myLocation);
            }
        } else {
            Toast.makeText(getActivity(), R.string.google_maps_not_supported, Toast.LENGTH_LONG).show();
        }

    }

    private void setMyLocationUpdatesEnabled(boolean enabled) {
        googleMap.setMyLocationEnabled(enabled);
        googleMap.setOnMyLocationChangeListener(enabled ? this : null);
    }

    @Override
    public void onMyLocationChange(Location location) {
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        showMyLocationMarker(myLocation);
        if (googleMap.getMyLocation() == null) {
            centerMapOnMyLocation(myLocation);
        }
    }

    private void centerMapOnMyLocation(LatLng myLocation) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM));
    }

    private void showMyLocationMarker(LatLng myLocation) {
//        if (myLocationMarker == null) {
//            myLocationMarker = googleMap.addMarker(new MarkerOptions()
//                    .position(myLocation)
//                    .title(getString(R.string.my_location))
//                    .snippet(getString(R.string.my_location_snippet)));
//        } else {
//            myLocationMarker.setPosition(myLocation);
//        }
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

    private void updatePlaceMarkers(List<Place> places) {
        googleMap.clear();
        showMyLocationMarker(myLocation);
        for (Place place : places) {
            PlaceLocation location = place.getGeometry().getLocation();
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(place.getName())
                    .snippet(place.getVicinity()));
        }
    }

}
