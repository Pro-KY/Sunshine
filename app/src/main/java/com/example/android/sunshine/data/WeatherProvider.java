package com.example.android.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

public class WeatherProvider extends ContentProvider {
    private WeatherDbHelper mWeatherDbHelper;

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;


    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        matcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER_WITH_DATE, CODE_WEATHER_WITH_DATE);
        return matcher;
    }

    public boolean onCreate() {
        Context context = getContext();
        // make connection to the database
        mWeatherDbHelper = new WeatherDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri,  String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mWeatherDbHelper.getReadableDatabase();
        int match = buildUriMatcher().match(uri);

        // returned cursor from the db query
        Cursor retCursor;

        switch (match) {
            case CODE_WEATHER:
                retCursor = db.query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CODE_WEATHER_WITH_DATE:
                // get the date string
                String utcDateString = uri.getPathSegments().get(1);

                retCursor = db.query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherEntry.COLUMN_DATE + " = ? ",  // selection
                        new String[] {utcDateString}, // selectionArgs
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    // add a bunch of weather data, not just one row.
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int matcher = buildUriMatcher().match(uri);
        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();

        int rowsInserted = 0;

        switch (matcher) {
            case CODE_WEATHER:
                db.beginTransaction();
                try {
                    for(ContentValues cv : values) {
                        long rowId = db.insert(WeatherEntry.TABLE_NAME, null, cv);
                        if(rowId != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsInserted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mWeatherDbHelper.getReadableDatabase();
        int match = buildUriMatcher().match(uri);

        int rowsDeleted;
        if(selection == null) selection = "1";

        switch (match) {
            case CODE_WEATHER:
                rowsDeleted = db.delete(
                        WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                 );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
