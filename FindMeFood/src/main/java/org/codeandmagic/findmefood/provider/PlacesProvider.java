package org.codeandmagic.findmefood.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.text.MessageFormat;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.provider.PlacesDatabase.Places;
import static org.codeandmagic.findmefood.provider.PlacesDatabase.URI_MATCHER;

/**
 * Created by evelyne24.
 */
public class PlacesProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;
    private ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        contentResolver = getContext().getContentResolver();
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case Places.ALL_ITEMS:
                return Places.CONTENT_TYPE;

            case Places.SINGLE_ITEM:
                return Places.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException(MessageFormat.format("Unsupported URI {0}.", uri));
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        if (isDatabaseOpen(sqLiteDatabase, true)) {

            final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            switch (URI_MATCHER.match(uri)) {
                case Places.ALL_ITEMS:
                    builder.setTables(Places.TABLE);
                    break;


                case Places.SINGLE_ITEM:
                    builder.setTables(Places.TABLE);
                    builder.appendWhere(Places._ID + "=" + uri.getLastPathSegment());
                    break;

                default:
                    throw new IllegalArgumentException(MessageFormat.format("Unsupported URI {0}.", uri));
            }

            final Cursor cursor = builder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
            if (null != cursor) {
                cursor.setNotificationUri(contentResolver, uri);
            }
            return cursor;

        } else {
            // Create an empty Cursor to avoid checking for null
            return new MatrixCursor(Places.PROJECTION);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        if (isDatabaseOpen(sqLiteDatabase, false)) {

            final long insertedId;
            switch (URI_MATCHER.match(uri)) {
                case Places.ALL_ITEMS:
                    insertedId = sqLiteDatabase.insert(Places.TABLE, null, values);
                    break;

                default:
                    throw new IllegalArgumentException(MessageFormat.format("Unsupported URI {0}.", uri));
            }

            if (insertedId >= 0) {
                contentResolver.notifyChange(uri, null);
            }
            return ContentUris.withAppendedId(Places.CONTENT_URI, insertedId);

        } else {
            Log.e(APP_TAG, "Cannot open writable database.");
            return Uri.EMPTY;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        if (isDatabaseOpen(sqLiteDatabase, false)) {

            final int rowsDeleted;
            switch (URI_MATCHER.match(uri)) {
                case Places.ALL_ITEMS:
                    rowsDeleted = sqLiteDatabase.delete(Places.TABLE, selection, selectionArgs);
                    break;

                case Places.SINGLE_ITEM:
                    rowsDeleted = sqLiteDatabase.delete(Places.TABLE, buildItemWhereClause(uri, selection), selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException(MessageFormat.format("Unsupported URI {0}.", uri));
            }

            if (rowsDeleted > 0) {
                contentResolver.notifyChange(uri, null);
            }
            return rowsDeleted;

        } else {
            Log.e(APP_TAG, "Cannot open writable database.");
            return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        if (isDatabaseOpen(sqLiteDatabase, false)) {

            final int rowsUpdated;
            switch (URI_MATCHER.match(uri)) {
                case Places.ALL_ITEMS:
                    rowsUpdated = sqLiteDatabase.update(Places.TABLE, values, selection, selectionArgs);
                    break;

                case Places.SINGLE_ITEM:
                    rowsUpdated = sqLiteDatabase.delete(Places.TABLE, buildItemWhereClause(uri, selection), selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException(MessageFormat.format("Unsupported URI {0}.", uri));
            }

            if (rowsUpdated > 0) {
                contentResolver.notifyChange(uri, null);
            }
            return rowsUpdated;

        } else {
            Log.e(APP_TAG, "Cannot open writable database.");
            return 0;
        }
    }

    private String buildItemWhereClause(Uri uri, String selection) {
        final String itemId = uri.getLastPathSegment();
        if (TextUtils.isEmpty(itemId)) {
            return selection;
        }

        final StringBuilder where = new StringBuilder(Places._ID).append("=").append(itemId);
        if (!TextUtils.isEmpty(selection)) {
            where.append(" AND ").append(selection);
        }

        return where.toString();
    }

    private boolean isDatabaseOpen(SQLiteDatabase sqLiteDatabase, boolean readonly) {
        if (!readonly) {
            return sqLiteDatabase != null && !sqLiteDatabase.isReadOnly();
        } else {
            return sqLiteDatabase != null;
        }
    }
}
