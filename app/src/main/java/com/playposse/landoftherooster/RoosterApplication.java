package com.playposse.landoftherooster;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.services.LocationScanningService;

/**
 * Implementation of {@link Application}.
 */
public class RoosterApplication extends Application {

    private static final String LOG_TAG = RoosterApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // Start with a fresh database when running for debug.
        if (BuildConfig.DEBUG) {
            getApplicationContext().deleteDatabase(RoosterDatabaseHelper.DB_NAME);
            Log.i(LOG_TAG, "onCreate: Reset RoosterDatabase");
        }

        // Import configuration on the first run.
//        ConfigurationImport.startImport(this);

        startService(new Intent(this, LocationScanningService.class));
    }
}
