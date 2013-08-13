package org.codeandmagic.findmefood.provider;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import org.codeandmagic.findmefood.model.Place;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.codeandmagic.findmefood.Consts.APP_TAG;

/**
 * Created by evelyne24.
 */
public final class PlacesDatabase {

    static final String NAME = "findmefood.db";
    static final int VERSION = 1;
    static final String AUTHORITY = "org.codeandmagic.findmefood";
    static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, Places.TABLE, Places.ALL_ITEMS);
        matcher.addURI(AUTHORITY, Places.TABLE + "/*", Places.SINGLE_ITEM);
        return matcher;
    }

    private PlacesDatabase() {
        // This should not be instantiated directly
    }


    public interface Places extends BaseColumns {
        String TABLE = "places";
        String ID = "id";
        String NAME = "name";
        String ICON_URL = "icon_url";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String RATING = "rating";
        String PRICE_LEVEL = "price_level";
        String VICINITY = "vicinity";
        String OPEN_NOW = "open_now";
        //TODO Better model the relation between places and types as many-to-many in a separate join table
        String TYPES = "types";

        Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "places");
        String CONTENT_TYPE = "vnd.android.cursor.dir/places";
        String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/place";

        int ALL_ITEMS = 0;
        int SINGLE_ITEM = 1;

        String[] PROJECTION = {_ID, ID, NAME, ICON_URL, LATITUDE, LONGITUDE, RATING, PRICE_LEVEL, VICINITY, TYPES, OPEN_NOW};
    }

    public static void insertPlaces(Context context, List<Place> places) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for(Place place : places) {
            ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(Places.CONTENT_URI)
                    .withValue(Places.ID, place.getId())
                    .withValue(Places.NAME, place.getName())
                    .withValue(Places.ICON_URL, place.getIconUrl())
                    .withValue(Places.VICINITY, place.getVicinity())
                    .withValue(Places.LATITUDE, place.getGeometry().getLocation().getLatitude())
                    .withValue(Places.LONGITUDE, place.getGeometry().getLocation().getLongitude())
                    .withValue(Places.PRICE_LEVEL, place.getPriceLevel())
                    .withValue(Places.RATING, place.getRating())
                    .withValue(Places.OPEN_NOW, place.getOpeningHours().isOpenNow() ? 1 : 0);

            List<String> types = place.getTypes();
            op.withValue(Places.TYPES, TextUtils.join(Place.TYPES_DELIMITER, types.toArray(new String[types.size()])));
            ops.add(op.build());
        }

        try {
            context.getContentResolver().applyBatch(PlacesDatabase.AUTHORITY, ops);
            Log.v(APP_TAG, MessageFormat.format("Inserted {0} Places into database.", places.size()));
        } catch (Exception e) {
            Log.e(APP_TAG, "Failed to insert Places into database.", e);
        }
    }
}
