package org.codeandmagic.findmefood.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.apache.http.HttpStatus;
import org.codeandmagic.findmefood.NetworkUtils;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;

import java.text.MessageFormat;
import java.util.List;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Http.STATUS_CODE_EXCEPTION;
import static org.codeandmagic.findmefood.Consts.Intents.*;
import static org.codeandmagic.findmefood.Consts.UNSET_VALUE;

/**
 * Created by evelyne24.
 */
public class UpdatePlacesService extends IntentService {

    private HttpClient httpClient;
    private HttpResponseParser responseParser;

    public UpdatePlacesService() {
        super(UpdatePlacesService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpClient = new HttpClient(this);
        responseParser = new HttpResponseParser();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, UNSET_VALUE);
        double longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, UNSET_VALUE);
        double radius = intent.getDoubleExtra(EXTRA_RADIUS, Place.DEFAULT_RADIUS);
        String[] types = intent.getStringArrayExtra(EXTRA_TYPES);

        HttpGetPlaces request = new HttpGetPlaces()
                .setLocation(latitude, longitude)
                .setRadius(radius)
                .setTypes(types);

        HttpResponse response = httpClient.executeRequest(request);
        if (HttpStatus.SC_OK != response.getStatusCode()) {
            broadcastResponseFailure(response);
        } else {
            List<Place> places = responseParser.parsePlaces(response.getResponse());
            Log.i(APP_TAG, MessageFormat.format("GET {0}:\nGot {1} places.", request.getUrl(), places.size()));

            if(places.size() > 0) {
                PlacesDatabase.insertPlaces(this, places);
            }
        }
    }

    private void broadcastResponseFailure(HttpResponse response) {
        Intent responseIntent;

        if (STATUS_CODE_EXCEPTION == response.getStatusCode()) {
            if (!NetworkUtils.isConnected(this)) {
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
}
