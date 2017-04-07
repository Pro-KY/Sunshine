package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.android.sunshine.data.WeatherContract;

// class to handle all of our synchronization
public class SunshineSyncUtils {

    // will be set when initialize is called for the first time
    public static boolean sInitialized;

    public static void startImmediateSync(final Context context) {
        Intent intentToSyncImmmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmmediately);
    }

    // check that startImmediateSync will only get called once when the app starts and only if
    // the database was empty
    synchronized public static void initialize(final Context context) {

        if(!sInitialized) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                    String selectionStatement = WeatherContract.WeatherEntry.
                            getSqlSelectForTodayOnwards();

                    Cursor cursor =  context.getContentResolver().query(
                            WeatherContract.WeatherEntry.CONTENT_URI,
                            projectionColumns,
                            selectionStatement,
                            null,
                            null
                    );

                    if(cursor == null || cursor.getCount() == 0) {
                        startImmediateSync(context);
                    }

                    cursor.close();
                    return null;
                }
            }.execute();
        }
    }
}
