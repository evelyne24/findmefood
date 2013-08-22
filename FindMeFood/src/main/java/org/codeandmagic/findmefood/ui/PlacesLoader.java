package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.support.v4.content.CursorLoader;

import com.google.android.gms.maps.model.LatLng;

import static org.codeandmagic.findmefood.Utils.*;

import static org.codeandmagic.findmefood.provider.PlacesDatabase.*;

/**
 * Created by evelyne24 on 22/08/2013.
 */
public class PlacesLoader extends CursorLoader {

    private Location location;
    private ForceLoadContentObserver observer;

    public PlacesLoader(Context context, Location location) {
        super(context, Places.CONTENT_URI, Places.PROJECTION, null, null, null);
        this.location = location;
        this.observer = new ForceLoadContentObserver();
    }

    @Override
    public Cursor loadInBackground() {
        final Cursor cursor =  super.loadInBackground();
        if(location == null) {
            return cursor;
        }

        final MatrixCursor newCursor = new MatrixCursor(Places.PROJECTION);
        while(cursor.moveToNext()) {
            final Object[] row = new Object[Places.PROJECTION.length];
            for (int i = 0; i < Places.PROJECTION.length; ++i) {
               row[i] = cursor.getString(cursor.getColumnIndex(Places.PROJECTION[i]));
            }
            double lat = cursor.getDouble(cursor.getColumnIndex(Places.LATITUDE));
            double lon = cursor.getDouble(cursor.getColumnIndex(Places.LONGITUDE));
            row[Places.PROJECTION.length - 1] = getDistanceBetween(location.getLatitude(), location.getLongitude(), lat, lon);
            newCursor.addRow(row);
        }
        cursor.close();

        newCursor.registerContentObserver(observer);
        return newCursor;
    }
}
