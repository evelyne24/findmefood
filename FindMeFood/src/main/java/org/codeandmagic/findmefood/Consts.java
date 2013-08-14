package org.codeandmagic.findmefood;

/**
 * Created by evelyne24.
 */
public interface Consts {

    String APP_TAG = "FindMeFood";
    String SPACE = " ";
    String NEWLINE = "\n";
    int UNSET = -1;

    interface Http {
        String GET = "GET";
        String ENCODING = "UTF-8";

        int STATUS_CODE_EXCEPTION = 5000;
        int READ_TIMEOUT = 10000; // milliseconds
        int CONNECT_TIMEOUT = 15000; // milliseconds
    }

    interface Intents {
        String ACTION_REQUEST_FAILED_NO_CONNECTION = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_NO_CONNECTION";
        String ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_UNEXPECTED_STATUS_CODE";
        String ACTION_REQUEST_FAILED_UNKNOWN = "org.codeandmagic.findmefood.ACTION_REQUEST_FAILED_UNKNOWN";

        String EXTRA_LATITUDE = "latitude";
        String EXTRA_LONGITUDE = "longitude";
        String EXTRA_RADIUS = "radius";
        String EXTRA_TYPES = "types";
        String EXTRA_STATUS_CODE = "status_code";
        String EXTRA_PLACE = "place";
    }

    interface SavedInstanceState {
        String MY_LOCATION = "my_location";
        String REQUESTED_LOCATION_UPDATES = "requested_location_updates";
    }

    //TODO Move these values inside a Settings screen
    interface Settings {
        int MILLISECONDS_PER_SECOND = 1000;
        int UPDATE_INTERVAL_IN_SECONDS = 5;
        int FAST_INTERVAL_IN_SECONDS = 1;
        long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
        long FAST_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_INTERVAL_IN_SECONDS;

    }

    interface Loaders {
        int LOAD_PLACES = 0;
    }
}
