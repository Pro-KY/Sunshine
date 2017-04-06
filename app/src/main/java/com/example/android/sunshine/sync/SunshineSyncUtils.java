package com.example.android.sunshine.sync;


import android.content.Context;
import android.content.Intent;

public class SunshineSyncUtils {

    public static void startImmediateSync(final Context context) {
        Intent intentToSyncImmmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmmediately);
    }
}
