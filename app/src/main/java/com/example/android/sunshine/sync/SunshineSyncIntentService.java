package com.example.android.sunshine.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

// create and register an IntentService that will be able to perform that task.
public class SunshineSyncIntentService  extends IntentService {
    public SunshineSyncIntentService() {
        super("SunshineSyncIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
