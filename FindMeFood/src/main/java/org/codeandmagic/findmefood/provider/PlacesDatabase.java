package org.codeandmagic.findmefood.provider;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import org.codeandmagic.findmefood.model.OpeningHours;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.model.PlaceGeometry;
import org.codeandmagic.findmefood.model.PlaceLocation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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

    private PlacesDatabase() {
        // This should not be instantiated directly
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, Places.TABLE, Places.ALL_ITEMS);
        matcher.addURI(AUTHORITY, Places.TABLE + "/*", Places.SINGLE_ITEM);
        return matcher;
    }

    public static void insertPlaces(Context context, List<Place> places) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (Place place : places) {
            ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(Places.CONTENT_URI)
                    .withValue(Places.ID, place.getId())
                    .withValue(Places.NAME, place.getName())
                    .withValue(Places.ICON_URL, place.getIconUrl())
                    .withValue(Places.VICINITY, place.getVicinity())
                    .withValue(Places.LATITUDE, place.getGeometry().getLocation().getLatitude())
                    .withValue(Places.LONGITUDE, place.getGeometry().getLocation().getLongitude())
                    .withValue(Places.PRICE_LEVEL, place.getPriceLevel())
                    .withValue(Places.RATING, place.getRating())
                    .withValue(Places.OPEN_NOW, place.getOpeningHours().isOpenNow() ? 1 : 0)
                    .withValue(Places.TYPES, place.getTypesAsString());
            ops.add(op.build());
        }

        try {
            context.getContentResolver().applyBatch(PlacesDatabase.AUTHORITY, ops);
            Log.v(APP_TAG, MessageFormat.format("Inserted {0} Places into database.", places.size()));
        } catch (Exception e) {
            Log.e(APP_TAG, "Failed to insert Places into database.", e);
        }
    }

    public static Place readPlace(Cursor cursor) {
        Place place = new Place();
        place.setId(cursor.getString(cursor.getColumnIndex(Places.ID)));
        place.setName(cursor.getString(cursor.getColumnIndex(Places.NAME)));
        place.setVicinity(cursor.getString(cursor.getColumnIndex(Places.VICINITY)));
        place.setIconUrl(cursor.getString(cursor.getColumnIndex(Places.ICON_URL)));
        place.setPriceLevel(cursor.getInt(cursor.getColumnIndex(Places.PRICE_LEVEL)));
        place.setRating(cursor.getDouble(cursor.getColumnIndex(Places.RATING)));
        place.setOpeningHours(new OpeningHours(cursor.getInt(cursor.getColumnIndex(Places.OPEN_NOW)) > 0));
        place.setGeometry(new PlaceGeometry(new PlaceLocation(cursor.getDouble(cursor.getColumnIndex(Places.LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(Places.LONGITUDE)))));
        place.setTypes(cursor.getString(cursor.getColumnIndex(Places.TYPES)));
        return place;
    }

    public static List<Place> readPlaces(Cursor cursor) {
        if (cursor == null) {
            return Collections.emptyList();
        }
        List<Place> places = new ArrayList<Place>();
        while (cursor.moveToNext()) {
            places.add(readPlace(cursor));
        }
        return places;
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
        String TYPES = "types";
        String[] PROJECTION = {_ID, ID, NAME, ICON_URL, LATITUDE, LONGITUDE, RATING, PRICE_LEVEL, VICINITY, TYPES, OPEN_NOW};

        Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "places");
        String CONTENT_TYPE = "vnd.android.cursor.dir/places";
        String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/place";

        int ALL_ITEMS = 0;
        int SINGLE_ITEM = 1;
    }
}
