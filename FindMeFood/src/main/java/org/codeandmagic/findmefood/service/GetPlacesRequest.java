package org.codeandmagic.findmefood.service;

import android.content.Context;
import android.text.TextUtils;
import org.codeandmagic.findmefood.R;

import java.text.MessageFormat;

/**
 * Created by evelyne24.
 */
public class GetPlacesRequest extends RequestBuilder {

    public static final String TYPE_BAR = "bar";
    public static final String TYPE_FOOD = "food";
    public static final String TYPE_CAFE = "cafe";
    public static final String TYPE_BAKERY = "bakery";

    private static final String PARAM_KEY = "key";
    private static final String PARAM_LOCATION = "location";
    private static final String PARAM_RADIUS = "radius";
    private static final String PARAM_SENSOR = "sensor";
    private static final String PARAM_TYPES = "types";

    private static final String LOCATION_FORMAT = "{0},{1}";
    private static final String TYPES_DELIMITER = "|";

    public GetPlacesRequest(Context context) {
        addParamName(PARAM_KEY, true)
        .addParamName(PARAM_LOCATION, true)
        .addParamName(PARAM_RADIUS, true)
        .addParamName(PARAM_TYPES, false)
        .addParamName(PARAM_SENSOR, false);

        addParam(PARAM_KEY, context.getString(R.string.google_places_api_key));
        addParam(PARAM_SENSOR, String.valueOf(true));
        addParam(PARAM_TYPES, TextUtils.join(TYPES_DELIMITER, new String[] {TYPE_BAR, TYPE_FOOD}));
    }

    public GetPlacesRequest setLocation(double latitude, double longitude) {
        addParam(PARAM_LOCATION, MessageFormat.format(LOCATION_FORMAT, latitude, longitude));
        return this;
    }

    public GetPlacesRequest setTypes(String...types) {
        if(types != null) {
            addParam(PARAM_TYPES, TextUtils.join(TYPES_DELIMITER, types));
        }
        return this;
    }
}
