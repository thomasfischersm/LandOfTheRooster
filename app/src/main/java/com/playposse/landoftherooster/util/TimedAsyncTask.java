package com.playposse.landoftherooster.util;

import android.os.AsyncTask;
import android.util.Log;

/**
 * An {@link AsyncTask} that writes the start, end, and duration to the log.
 */
public abstract class TimedAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String LOG_TAG = getClass().getSimpleName();

    private long totalDuration = 0; // TODO remove after debugging.

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(LOG_TAG, "doInBackground: Started");
        long start = System.currentTimeMillis();

        doInBackground();

        long end = System.currentTimeMillis();
        totalDuration += (end - start);
        Log.d(LOG_TAG, "doInBackground: Ended " + (end - start) + "ms");
        Log.d(LOG_TAG, "doInBackground: Total processing time: " + (totalDuration / 1_000));

        return null;
    }

    protected  abstract  void doInBackground();
}
