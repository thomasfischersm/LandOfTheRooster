package com.playposse.landoftherooster.util;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * A little boilerplate that makes it easier to create {@link AsyncTask}s with weak references
 * back to the caller.
 *
 * @param <C> Gives the caller a reference back to itself.
 */
public abstract class WeakReferenceAsyncTask<C> extends TimedAsyncTask {

    private final WeakReference<C> weakReferenceCaller;

    protected WeakReferenceAsyncTask(C caller) {
        weakReferenceCaller = new WeakReference<>(caller);
    }

    @Override
    protected void doInBackground() {
        C caller = weakReferenceCaller.get();
        if (caller != null) {
            doInBackground(caller);
        }
    }

    protected abstract void doInBackground(C caller);
}
