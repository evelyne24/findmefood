package org.codeandmagic.findmefood.service;

import android.util.Log;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.codeandmagic.findmefood.model.Place;

import java.util.Collections;
import java.util.List;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.EMPTY;
import static org.codeandmagic.findmefood.Consts.Parser.KEY_NEXT_PAGE_TOKEN;
import static org.codeandmagic.findmefood.Consts.Parser.KEY_RESULTS;

/**
 * Created by evelyne24.
 */
public class HttpResponseParser {


    private Gson gson;
    private JsonParser parser;

    public HttpResponseParser() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        parser = new JsonParser();
    }

    public PlaceParseResponse parsePlaces(String response) {
        List<Place> places;
        String nextPageToken;
        try {
            JsonObject json = parser.parse(response).getAsJsonObject();
            JsonArray array = json.getAsJsonArray(KEY_RESULTS);
            places = gson.fromJson(array, new TypeToken<List<Place>>() {}.getType());

            if (json.has(KEY_NEXT_PAGE_TOKEN)) {
                nextPageToken = json.get(KEY_NEXT_PAGE_TOKEN).getAsString();
            } else {
                nextPageToken = EMPTY;
            }

        } catch (Exception e) {
            places = Collections.emptyList();
            nextPageToken = EMPTY;
            Log.w(APP_TAG, "Parse exception.", e);
        }
        return new PlaceParseResponse(places, nextPageToken);
    }

    public static class PlaceParseResponse {
        public final List<Place> places;
        public final String nextPageToken;

        public PlaceParseResponse(List<Place> places, String nextPageToken) {
            this.places = places;
            this.nextPageToken = nextPageToken;
        }
    }
}
