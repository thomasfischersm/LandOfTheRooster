package com.playposse.landoftherooster.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A {@link FutureTask} that waits for the user to grant location permission.
 */
public class LocationPermissionFutureTask extends FutureTask<Void> {

    private static final String LOG_TAG = LocationPermissionFutureTask.class.getSimpleName();

    private static final long POLLING_INTERVAL = 1_000;

    public LocationPermissionFutureTask(final Context context) {
        super(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while (true) {
                    Log.d(LOG_TAG, "call: About to check permissions");
                    if (hasLocationPermission(context)) {
                        Log.d(LOG_TAG, "call: Got permissions.");
                        return null;
                    }

                    Thread.sleep(POLLING_INTERVAL);
                }
            }
        });
    }

    private static boolean hasLocationPermission(Context context) {
        return ContextCompat
                .checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
    }
}
