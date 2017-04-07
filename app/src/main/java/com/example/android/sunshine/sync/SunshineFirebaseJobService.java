package com.example.android.sunshine.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

// JobService for syncing data from the server
public class SunshineFirebaseJobService extends JobService {
    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    @Override
    public boolean onStartJob(final JobParameters jobParams) {
        mFetchWeatherTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void...voids) {
                Context context = SunshineFirebaseJobService.this;
                SunshineSyncTask.syncWeather(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParams, false);
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(mFetchWeatherTask != null) mFetchWeatherTask.cancel(true);
        // rescheduled to finish that work that we were doing when you so rudely interrupted us.”
        return true;
    }
}
