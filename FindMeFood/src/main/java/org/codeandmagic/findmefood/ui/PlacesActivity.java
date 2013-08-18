package org.codeandmagic.findmefood.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import org.codeandmagic.findmefood.R;

import org.codeandmagic.findmefood.service.PlacesUpdateService;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Intents.*;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.*;
import static org.codeandmagic.findmefood.Consts.UserSettings.HIGH_PRIORITY_FAST_INTERVAL;
import static org.codeandmagic.findmefood.Consts.UserSettings.HIGH_PRIORITY_UPDATE_INTERVAL;

import static org.codeandmagic.findmefood.LocationUtils.*;

public class PlacesActivity extends ActionBarActivity implements LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
    */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location myLocation;
    private LocationClient locationClient;
    private LocationRequest singleUpdateRequest;
    private PlacesReceiver placesReceiver;
    private IntentFilter placesFilter;
    private boolean locationUpdateRequestInProgress = true;
    private boolean placesUpdateRequestInProgress = false;
    private boolean hasNextPage;

    public void requestNextPage() {
        if (myLocation != null && hasNextPage) {
            Log.d(APP_TAG, "Requesting next page of Places...");
            startPlacesUpdateService(myLocation);
        }
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.places_list_activity);
        setProgressBarIndeterminate(true);
        setSupportProgressBarVisibility(false);

        placesFilter = new IntentFilter(ACTION_REQUEST_SUCCESS);
        placesFilter.addAction(ACTION_REQUEST_FAILED_NO_CONNECTION);
        placesFilter.addAction(ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE);
        placesFilter.addAction(ACTION_REQUEST_FAILED_UNKNOWN);
        placesReceiver = new PlacesReceiver();

        if (savedInstanceState == null) {
            attachFragments();
        } else {
            restoreInstanceState(savedInstanceState);
        }

        setupLocationClient();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        myLocation = savedInstanceState.getParcelable(MY_LOCATION);
        placesUpdateRequestInProgress = savedInstanceState.getBoolean(PLACES_UPDATE_IN_PROGRESS, false);
        locationUpdateRequestInProgress = savedInstanceState.getBoolean(LOCATION_UPDATE_IN_PROGRESS, false);
        hasNextPage = savedInstanceState.getBoolean(HAS_NEXT_PLACES, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MY_LOCATION, myLocation);
        outState.putBoolean(PLACES_UPDATE_IN_PROGRESS, placesUpdateRequestInProgress);
        outState.putBoolean(LOCATION_UPDATE_IN_PROGRESS, locationUpdateRequestInProgress);
        outState.putBoolean(HAS_NEXT_PLACES, hasNextPage);
        super.onSaveInstanceState(outState);
    }

    private void setupLocationClient() {
        if (isGooglePlayServicesAvailable()) {
            locationClient = new LocationClient(this, this, this);
            locationClient.connect();

            singleUpdateRequest = LocationRequest.create();
            singleUpdateRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            singleUpdateRequest.setFastestInterval(HIGH_PRIORITY_FAST_INTERVAL);
            singleUpdateRequest.setInterval(HIGH_PRIORITY_UPDATE_INTERVAL);
            singleUpdateRequest.setNumUpdates(1);
        }
    }

    private void attachFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ft.replace(R.id.places_list_fragment, PlacesListFragment.newInstance(Bundle.EMPTY));

        if (isDualPane()) {
            //ft.replace(R.id.map_fragment, PlacesMapFragment.newInstance(Bundle.EMPTY));
            ft.replace(R.id.map_fragment, PlacesMapFragmentWithCache.newInstance(Bundle.EMPTY));
        }

        ft.commit();
    }

    private boolean isDualPane() {
        return findViewById(R.id.map_fragment) != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(placesReceiver, placesFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(placesReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
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
                onActionReloadClicked();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onActionReloadClicked() {
        if (!placesUpdateRequestInProgress) {
            // Mark that we're starting a Place update request
            placesUpdateRequestInProgress = true;
            Log.v(APP_TAG, "Starting a new Places update request...");

            // If Google Play Services is available
            if (isGooglePlayServicesAvailable()) {

                // If we haven't requested an update already
                if (!locationUpdateRequestInProgress) {
                    Log.v(APP_TAG, "There is no Location update request in progress and ...");

                    // If the LocationClient has connected successfully
                    if (locationClient.isConnected()) {
                        Log.v(APP_TAG, "... LocationClient is successfully connected.");

                        // Ask for a Location update
                        requestSingleLocationUpdate();
                    } else {
                        // Wait until LocationClient becomes connected
                        Log.w(APP_TAG, "Still waiting to connect to Google Play Services...");
                        Toast.makeText(this, R.string.waiting_to_connect_to_play_services, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Log.w(APP_TAG, "There's another Location update in progress, so wait for it to finish first.");
                }
            }

        } else {
            Log.v(APP_TAG, "There's another Places update in progress, so wait for it to finish first.");
            Toast.makeText(this, R.string.update_already_requested, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(APP_TAG, " --- LocationClient CONNECTED ---");

        // If we have requested a Location update
        if (locationUpdateRequestInProgress) {
            // Mark that we're starting a Location update
            locationUpdateRequestInProgress = true;
            Log.v(APP_TAG, "Starting a new Location update request.");

            // If we have requested a Location update as part of a Place request
            if (placesUpdateRequestInProgress) {
                // We require a fresh Location
                Log.v(APP_TAG, "There is a Places update request in progress.");
                requestSingleLocationUpdate();
            }
            // If we don't have any Location available
            else if (myLocation == null) {
                Log.v(APP_TAG, "The current Location is unknown.");
                // We try to get hold of an existing location that's good enough to get Places
                Location lastLocation = locationClient.getLastLocation();
                if (isLocationFresh(lastLocation)) {
                    // The last known Location is good enough
                    Log.v(APP_TAG, "The last known Location is fresh enough to use.");

                    myLocation = lastLocation;
                    startPlacesUpdateService(lastLocation);
                }
                else {
                    // The Last known location is not good enough
                    Log.v(APP_TAG, "The last known Location is not fresh enough to use.");
                    requestSingleLocationUpdate();
                }
            }
        }
    }

    private void requestSingleLocationUpdate() {
        locationClient.requestLocationUpdates(singleUpdateRequest, this);
        Log.d(APP_TAG, " --- REQUESTED LOCATION UPDATE ---");
    }

    private void stopLocationUpdates() {
        if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
            locationClient.disconnect();
            Log.v(APP_TAG, "We've stopped listening for Location updates.");
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(APP_TAG, "--- LocationClient DISCONNECTED ---");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(APP_TAG, "=== LOCATION CHANGED " + location);

            myLocation = location;
            locationUpdateRequestInProgress = false;
            stopLocationUpdates();

            // Update Places based on the new Location
            startPlacesUpdateService(location);

        } else {
            Log.w(APP_TAG, "Received a null location.");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(APP_TAG, " --- LocationClient FAILED to connect ---");
        Toast.makeText(this, R.string.google_play_services_connection_failed, Toast.LENGTH_LONG).show();
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        Log.d(APP_TAG, "--- Google Play Services CONNECTED ---");
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        Log.w(APP_TAG, "---- Google Play Services FAILED to connect ---");

                        // Display the result
                        Toast.makeText(this, R.string.google_play_services_connection_failed, Toast.LENGTH_LONG).show();
                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.w(APP_TAG, "Unknown request code: " + requestCode);
                break;
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        boolean success = (ConnectionResult.SUCCESS == resultCode);
        if (!success) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), APP_TAG);
            }
        }
        return success;
    }

    private void startPlacesUpdateService(Location location) {
        // Show a progress bar in the ActionBar
        setSupportProgressBarVisibility(true);

        Intent serviceIntent = new Intent(this, PlacesUpdateService.class);
        serviceIntent.putExtra(EXTRA_LATITUDE, location.getLatitude());
        serviceIntent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
        startService(serviceIntent);
        Log.d(APP_TAG, "--- PlacesUpdateService STARTED ---");
    }

    private class PlacesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_REQUEST_SUCCESS.equals(action) ||
                    ACTION_REQUEST_FAILED_NO_CONNECTION.equals(action) ||
                    ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE.equals(action) ||
                    ACTION_REQUEST_FAILED_UNKNOWN.equals(action)) {

                // Hide the progress bar in the ActionBar
                setSupportProgressBarVisibility(false);
                placesUpdateRequestInProgress = false;

                // TODO show error dialogs if needed
            }
            if(ACTION_REQUEST_SUCCESS.equals(action)) {
                hasNextPage = intent.getBooleanExtra(EXTRA_HAS_NEXT_PAGE, false);
            }
        }
    }

}
