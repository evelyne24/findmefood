package org.codeandmagic.findmefood.service;

import android.util.Log;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.codeandmagic.findmefood.model.Place;

import java.util.Collections;
import java.util.List;

import static org.codeandmagic.findmefood.Consts.APP_TAG;

/**
 * Created by evelyne24.
 */
public class HttpResponseParser {

    private static final String KEY_RESULTS = "results";
    private Gson gson;
    private JsonParser parser;

    public HttpResponseParser() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        parser = new JsonParser();
    }

    public List<Place> parsePlaces(String response) {
        try {
            JsonObject json = parser.parse(response).getAsJsonObject();
            JsonArray array = json.getAsJsonArray(KEY_RESULTS);
            return gson.fromJson(array, new TypeToken<List<Place>>() {}.getType());
        } catch (Exception e) {
            Log.w(APP_TAG, "Parse exception.", e);
            return Collections.emptyList();
        }
    }
}
