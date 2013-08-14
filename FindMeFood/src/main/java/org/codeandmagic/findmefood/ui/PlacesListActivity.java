package org.codeandmagic.findmefood.ui;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.service.UpdatePlacesService;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Intents.EXTRA_LATITUDE;
import static org.codeandmagic.findmefood.Consts.Intents.EXTRA_LONGITUDE;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.MY_LOCATION;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.REQUESTED_LOCATION_UPDATES;
import static org.codeandmagic.findmefood.Consts.Settings.FAST_INTERVAL_IN_MILLISECONDS;
import static org.codeandmagic.findmefood.Consts.Settings.UPDATE_INTERVAL_IN_MILLISECONDS;

public class PlacesListActivity extends ActionBarActivity implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {


    private boolean requestedLocationUpdates;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_list_activity);

        if (savedInstanceState == null) {
            attachFragments();
        } else {
            restoreState(savedInstanceState);
        }

        setupLocationClient();
    }

    private void attachFragments() {
        Fragment placesListFragment = PlacesListFragment.instantiate(this, PlacesListFragment.class.getName());
        getSupportFragmentManager().beginTransaction().replace(R.id.places_list_fragment, placesListFragment).commit();

        boolean dualPane = (findViewById(R.id.map_fragment) != null);
        if(dualPane) {
            Fragment mapFragment = PlacesMapFragment.instantiate(this, PlacesMapFragment.class.getName());
            getSupportFragmentManager().beginTransaction().replace(R.id.map_fragment, mapFragment).commit();
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        requestedLocationUpdates = savedInstanceState.getBoolean(REQUESTED_LOCATION_UPDATES, true);
        myLocation = savedInstanceState.getParcelable(MY_LOCATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MY_LOCATION, myLocation);
        outState.putBoolean(REQUESTED_LOCATION_UPDATES, requestedLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    private void setupLocationClient() {
        locationClient = new LocationClient(this, this, this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_IN_MILLISECONDS);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();
        super.onStop();
    }

    private void startPeriodicUpdates() {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    private void stopPeriodicUpdates() {
        locationClient.removeLocationUpdates(this);
    }

    private void startLocationUpdates() {
        requestedLocationUpdates = true;
        if (playServicesConnected()) {
            startPeriodicUpdates();
        }
    }

    private void stopLocationUpdates() {
        requestedLocationUpdates = false;
        if (playServicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(APP_TAG, "LocationClient connected.");

        if (myLocation == null) {
            Location lastLocation = locationClient.getLastLocation();

            if (lastLocation != null) {
                myLocation = lastLocation;
                getPlacesForMyLocation(myLocation);
            } else if (!requestedLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onDisconnected() {
        Log.v(APP_TAG, "LocationClient disconnected.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(APP_TAG, "Location update: " + myLocation);
        if (location != null && myLocation == null) {
            myLocation = location;
            getPlacesForMyLocation(myLocation);
            stopLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean playServicesConnected() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        final boolean playServicesAvailable = (ConnectionResult.SUCCESS == resultCode);

        if (playServicesAvailable) {
            Log.v(APP_TAG, "Google Play Services available.");
        } else {
            Log.w(APP_TAG, "Google Play Services not available.");
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), APP_TAG);
            }
        }
        return playServicesAvailable;
    }

    private void getPlacesForMyLocation(Location location) {
        Intent serviceIntent = new Intent(this, UpdatePlacesService.class);
        serviceIntent.putExtra(EXTRA_LATITUDE, location.getLatitude());
        serviceIntent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
        startService(serviceIntent);
    }

}
