package org.codeandmagic.findmefood.service;

import android.text.TextUtils;
import org.codeandmagic.findmefood.model.Place;

import java.text.MessageFormat;

import static org.codeandmagic.findmefood.Consts.UNSET;
import static org.codeandmagic.findmefood.Consts.UserSettings.DEFAULT_RADIUS;

/**
 * Created by evelyne24.
 */
public class HttpGetPlaces extends HttpRequest {



    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String PARAM_LOCATION = "location";
    private static final String PARAM_RADIUS = "radius";
    private static final String PARAM_SENSOR = "sensor";
    private static final String PARAM_TYPES = "types";
    private static final String PARAM_NEXT_PAGE_TOKEN = "pagetoken";
    private static final String LOCATION_FORMAT = "{0},{1}";

    public HttpGetPlaces() {
        // Specify the required params for this request
        addParamName(PARAM_LOCATION, true)
        .addParamName(PARAM_RADIUS, true)
        .addParamName(PARAM_TYPES, false)
        .addParamName(PARAM_SENSOR, false);

        // Specify the default param values for this request
        addParam(PARAM_SENSOR, String.valueOf(true));
        addParam(PARAM_RADIUS, String.valueOf(DEFAULT_RADIUS));
        addParam(PARAM_TYPES, Place.DEFAULT_TYPES);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    public HttpGetPlaces setLocation(double latitude, double longitude) {
        if(Double.compare(UNSET, latitude) == 0) {
            throw new MalformedRequestException("Missing required param 'latitude'.");
        }
        if(Double.compare(UNSET, longitude) == 0) {
            throw new MalformedRequestException("Missing required param 'longitude'.");
        }
        addParam(PARAM_LOCATION, MessageFormat.format(LOCATION_FORMAT, latitude, longitude));
        return this;
    }

    public HttpGetPlaces setRadius(double radius) {
        if(0 > radius) {
            throw new MalformedRequestException(MessageFormat.format("Invalid required param 'radius': {0}.", radius));
        }
        addParam(PARAM_RADIUS, Double.toString(radius));
        return this;
    }

    public HttpGetPlaces setTypes(String types) {
        if(!TextUtils.isEmpty(types)) {
            addParam(PARAM_TYPES, types);
        }
        return this;
    }

    public HttpGetPlaces setNextPageToken(String nextPageToken) {
        if(!TextUtils.isEmpty(nextPageToken)) {
            addParam(PARAM_NEXT_PAGE_TOKEN, nextPageToken);
        }
        return this;
    }
}
