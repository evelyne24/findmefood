package org.codeandmagic.findmefood.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.HttpStatus;
import org.codeandmagic.findmefood.Utils;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;
import org.codeandmagic.findmefood.service.HttpResponseParser.PlaceParseResponse;

import java.text.MessageFormat;

import static org.codeandmagic.findmefood.Consts.*;
import static org.codeandmagic.findmefood.Consts.Http.STATUS_CODE_EXCEPTION;
import static org.codeandmagic.findmefood.Consts.Intents.*;
import static org.codeandmagic.findmefood.Consts.SavedSharedPreferences.*;
import static org.codeandmagic.findmefood.Consts.UserSettings.DEFAULT_RADIUS;

/**
 * Created by evelyne24.
 */
public class PlacesUpdateService extends IntentService {

    // Google Place API will return 60 places for a location, each request will return 20 items.
    private static final int TOTAL_ITEMS = 60;

    private HttpClient httpClient;
    private HttpResponseParser responseParser;
    private SharedPreferences sharedPreferences;

    public PlacesUpdateService() {
        super(PlacesUpdateService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpClient = new HttpClient(this);
        responseParser = new HttpResponseParser();
        sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, UNSET);
        double longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, UNSET);
        double radius = intent.getDoubleExtra(EXTRA_RADIUS, DEFAULT_RADIUS);
        String placeTypes = intent.getStringExtra(EXTRA_TYPES);
        String nextPageToken = sharedPreferences.getString(NEXT_PAGE_TOKEN, EMPTY);

        boolean hasNextPage = !TextUtils.isEmpty(nextPageToken);
        boolean hasLocationChangedEnough = hasLocationChangedEnough(latitude, longitude, radius, placeTypes);
        Log.v(APP_TAG, "Has next page token? " + hasNextPage + " -> " + nextPageToken);
        Log.v(APP_TAG, "Has Location changed enough? " + hasLocationChangedEnough);

        HttpRequest request = null;
        if (hasNextPage) {
            request = new HttpGetPlaces().setNextPageToken(nextPageToken);
        } else if (hasLocationChangedEnough) {
            request = new HttpGetPlaces().setLocation(latitude, longitude).setRadius(radius).setTypes(placeTypes);
        }

        if (request != null) {
            Log.d(APP_TAG, "Executing a Places update request...");

            HttpResponse response = httpClient.executeRequest(request);
            if (HttpStatus.SC_OK != response.getStatusCode()) {
                broadcastResponseFailure(response);
            } else {
                PlaceParseResponse parseResponse = responseParser.parsePlaces(response.getResponse());
                Log.i(APP_TAG, MessageFormat.format("GET {0}:\nGot {1} places.", request.getUrl(), parseResponse.places.size()));

                if (parseResponse.places.size() > 0) {
                    PlacesDatabase.insertPlaces(this, parseResponse.places);
                }

                boolean hasNextToken = !TextUtils.isEmpty(parseResponse.nextPageToken);
                if (hasNextToken) {
                    saveNextPageToken(parseResponse.nextPageToken);
                }
                broadcastResponseSuccess(hasNextToken);
            }

            saveLastUpdateRequestExtras(latitude, longitude, radius, placeTypes);
        } else {
            Log.d(APP_TAG, "There's no more data to load or a similar request has already been processed.");
        }
    }

    private boolean hasLocationChangedEnough(double latitude, double longitude, double radius, String types) {
        double savedLatitude = Utils.coordinateFromInt(sharedPreferences.getInt(LATITUDE, 0));
        double savedLongitude = Utils.coordinateFromInt(sharedPreferences.getInt(LONGITUDE, 0));
        double savedRadius = sharedPreferences.getFloat(RADIUS, 0);
        String savedTypes = sharedPreferences.getString(PLACE_TYPES, Place.DEFAULT_TYPES);

        boolean outsideRadius = Utils.getDistanceBetween(savedLatitude, savedLongitude, latitude, longitude) > DEFAULT_RADIUS;
        boolean hasChangedRadius = (savedRadius - radius) > 1;
        boolean hasChangedTypes = !savedTypes.equals(types);

        return outsideRadius || hasChangedRadius || hasChangedTypes;
    }

    private void saveNextPageToken(String nextPageToken) {
        sharedPreferences.edit().putString(NEXT_PAGE_TOKEN, nextPageToken).commit();
    }

    private void saveLastUpdateRequestExtras(double latitude, double longitude, double radius, String types) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LATITUDE, Utils.coordinateToInt(latitude))
                .putInt(LONGITUDE, Utils.coordinateToInt(longitude))
                .putFloat(RADIUS, (float) radius)
                .putString(PLACE_TYPES, !TextUtils.isEmpty(types) ? types : Place.DEFAULT_TYPES)
                .commit();
    }

    private void broadcastResponseFailure(HttpResponse response) {
        Intent responseIntent;

        if (STATUS_CODE_EXCEPTION == response.getStatusCode()) {
            if (!Utils.isConnected(this)) {
                responseIntent = new Intent(ACTION_REQUEST_FAILED_NO_CONNECTION);
            } else {
                responseIntent = new Intent(ACTION_REQUEST_FAILED_UNKNOWN);
            }
        } else {
            responseIntent = new Intent(ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE);
            responseIntent.putExtra(EXTRA_STATUS_CODE, response.getStatusCode());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
    }

    private void broadcastResponseSuccess(boolean hasNextToken) {
        Intent responseIntent = new Intent(ACTION_REQUEST_SUCCESS);
        responseIntent.putExtra(EXTRA_HAS_NEXT_PAGE, hasNextToken);
        responseIntent.putExtra(EXTRA_TOTAL_ITEMS, TOTAL_ITEMS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
    }
}
