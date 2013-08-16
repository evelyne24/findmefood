package org.codeandmagic.findmefood;

/**
 * Created by evelyne24.
 */
public interface Consts {

    String APP_TAG = "FindMeFood";
    String EMPTY = "";
    String SPACE = " ";

    int UNSET = -1;
    int MILLIS_PER_SECOND = 1000;
    double IE6 = 1e6;

    interface Http {
        String GET = "GET";
        String ENCODING = "UTF-8";
        int STATUS_CODE_EXCEPTION = 5000;
        int READ_TIMEOUT = 10000; // milliseconds
        int CONNECT_TIMEOUT = 15000; // milliseconds
    }

    interface Parser {
        String KEY_RESULTS = "results";
        String KEY_NEXT_PAGE_TOKEN = "next_page_token";
    }

    interface Intents {
        String ACTION_REQUEST_SUCCESS = "org.codeandmagic.findmefood.ACTION_REQUEST_SUCCESS";
        String ACTION_REQUEST_FAILED_NO_CONNECTION = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_NO_CONNECTION";
        String ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE";
        String ACTION_REQUEST_FAILED_UNKNOWN = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_UNKNOWN";
        String EXTRA_LATITUDE = "latitude";
        String EXTRA_LONGITUDE = "longitude";
        String EXTRA_RADIUS = "radius";
        String EXTRA_TYPES = "types";
        String EXTRA_STATUS_CODE = "status_code";
        String EXTRA_PLACE = "place";
        String EXTRA_HAS_NEXT_PAGE = "has_next_page";
        String EXTRA_TOTAL_ITEMS = "total_items";
    }

    //TODO Move these values inside a UserSettings screen
    interface UserSettings {
        long HIGH_PRIORITY_UPDATE_INTERVAL = 5 * MILLIS_PER_SECOND;
        long HIGH_PRIORITY_FAST_INTERVAL = MILLIS_PER_SECOND;
        long LOW_PRIORITY_UPDATE_INTERVAL = 60 * 60 * MILLIS_PER_SECOND;
        long LOW_PRIORITY_FAST_INTERVAL = 5 * 60 * MILLIS_PER_SECOND;
        long FRESH_LOCATION_INTERVAL =  60 * MILLIS_PER_SECOND;
        double DEFAULT_RADIUS = 1609.344; // 1 mile in meters
    }

    interface SavedSharedPreferences {
        String PREFERENCES = "FindMeFoodPrefs";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String RADIUS = "radius";
        String PLACE_TYPES = "types";
        String NEXT_PAGE_TOKEN = "next_page_token";
    }

    interface SavedInstanceState {
        String MY_LOCATION = "my_location";
        String LOCATION_UPDATE_IN_PROGRESS = "location_update_in_progress";
        String PLACES_UPDATE_IN_PROGRESS = "places_update_in_progress";
        String HAS_NEXT_PLACES = "has_next_places";
    }

    interface Loaders {
        int LOAD_PLACES = 0;
    }
}
