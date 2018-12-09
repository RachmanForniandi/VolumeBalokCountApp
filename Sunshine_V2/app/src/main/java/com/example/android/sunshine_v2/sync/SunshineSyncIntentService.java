package com.example.android.sunshine_v2.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by USER on 20/03/2018.
 */

public class SunshineSyncIntentService extends IntentService {

    public SunshineSyncIntentService(){
        super("SunshineSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
