package com.playposse.landoftherooster.util;

import android.os.AsyncTask;
import android.util.Log;

/**
 * An {@link AsyncTask} that writes the start, end, and duration to the log.
 */
public abstract class TimedAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(LOG_TAG, "doInBackground: Started");
        long start = System.currentTimeMillis();

        doInBackground();

        long end = System.currentTimeMillis();
        Log.i(LOG_TAG, "doInBackground: Ended " + (end - start) + "ms");

        return null;
    }

    protected  abstract  void doInBackground();
}
