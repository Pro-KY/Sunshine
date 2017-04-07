package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.sunshine.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

// class to handle all of our synchronization
public class SunshineSyncUtils {

    private static final int SYNC_INTERVAL_HOURS = 3; // four hours
    private static final int SYNC_INTERVAL_SECONDS = (int) (TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS));
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static final String SUNSHINE_SYNC_TAG = "sunshine_sync";

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

            sInitialized = true;

            Thread checkForEmpty = new Thread(new Runnable() {

                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry.
                        getSqlSelectForTodayOnwards();

                @Override
                public void run() {
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
                }
            });

            checkForEmpty.start();
        }
    }

    synchronized public static void scheduleFirebaseJobDispacherSync(@NonNull final Context context) {

        if (sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        /* Create the Job to periodically create reminders to drink water */
        Job syncSunshineJob = dispatcher.newJobBuilder()
                /* The Service that will be used to write to preferences */
                .setService(SunshineFirebaseJobService.class)
                .setTag(SUNSHINE_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        /* Schedule the Job with the dispatcher */
        dispatcher.schedule(syncSunshineJob);
    }
}
