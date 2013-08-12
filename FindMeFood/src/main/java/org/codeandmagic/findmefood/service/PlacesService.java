package org.codeandmagic.findmefood.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import static org.codeandmagic.findmefood.model.Consts.*;

/**
 * Created by evelyne24.
 */
public class PlacesService extends IntentService {

    private static final String TAG = PlacesService.class.getName();
    private static final String NAME = PlacesService.class.getName();

    public PlacesService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double latitude = intent.getDoubleExtra(LATITUDE, -1);
        double longitude = intent.getDoubleExtra(LONGITUDE, -1);
        int radius = intent.getIntExtra(RADIUS, -1);
        String[] types = intent.getStringArrayExtra(TYPES);

        if(latitude == -1 || longitude == -1) {
            Log.e(TAG, MessageFormat.format("Bad latitude/longitude: {0}, {1}.", latitude, longitude));
            return;
        }
        if(radius == -1) {
            Log.e(TAG, MessageFormat.format("Bad radius: {0}", radius));
            return;
        }

        GetPlacesRequest request = new GetPlacesRequest(this);
        request.setLocation(latitude, longitude);
        request.setTypes(types);


    }

    private String getPlaces(GetPlacesRequest request) {
        URL url = new URL(request.build());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        int response = conn.getResponseCode();
        Log.d(DEBUG_TAG, "The response is: " + response);
        is = conn.getInputStream();
    }
}
