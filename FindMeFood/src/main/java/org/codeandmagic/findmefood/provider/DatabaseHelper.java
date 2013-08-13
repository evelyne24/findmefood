package org.codeandmagic.findmefood.provider;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.codeandmagic.findmefood.model.Place;

import java.text.MessageFormat;
import java.util.ArrayList;

import static org.codeandmagic.findmefood.provider.PlacesDatabase.Places;
import static org.codeandmagic.findmefood.Consts.APP_TAG;

/**
 * Created by evelyne24.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, PlacesDatabase.NAME, null, PlacesDatabase.VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createPlacesTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Places.TABLE);
            onCreate(db);
        }
    }



    private static String createPlacesTable() {
        return "CREATE TABLE " + Places.TABLE + " ( " +
                // Columns
                Places._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Places.ID               + " TEXT NOT NULL, " +
                Places.NAME             + " TEXT, " +
                Places.ICON_URL         + " TEXT, " +
                Places.VICINITY         + " TEXT, " +
                Places.TYPES            + " TEXT NOT NULL, " +
                Places.LATITUDE         + " REAL NOT NULL, " +
                Places.LONGITUDE        + " REAL NOT NULL, " +
                Places.RATING           + " REAL DEFAULT 0, " +
                Places.PRICE_LEVEL      + " INTEGER DEFAULT 0, " +
                Places.OPEN_NOW         + " INTEGER DEFAULT 0, " +
                // Indexes
                "UNIQUE (" + Places.ID + ") ON CONFLICT REPLACE );";
    }


}
