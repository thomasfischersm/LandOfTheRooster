package com.playposse.landoftherooster.util;

import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * An {@link Observer} that executes the result on a background thread of an {@link AsyncTask}.
 */
public abstract class AsyncObserver<C, T> implements Observer<T> {

    private static final String LOG_TAG = AsyncObserver.class.getSimpleName();

    private final WeakReference<C> callerRef;

    protected AsyncObserver(C caller) {
        callerRef = new WeakReference<>(caller);
    }

    @Override
    public void onChanged(@Nullable T t) {
        new LocalAsyncTask<>(this, callerRef)
                .execute();
    }

    protected abstract void onChangedAsync(C caller);

    private static class LocalAsyncTask<C, T> extends WeakReferenceAsyncTask<AsyncObserver<C, T>> {

        private final WeakReference<C> callerRef;

        private LocalAsyncTask(AsyncObserver<C, T> observer, WeakReference<C> callerRef) {
            super(observer);

            this.callerRef = callerRef;
        }

        @Override
        protected void doInBackground(AsyncObserver<C, T> observer) {
            Log.d(LOG_TAG, "doInBackground: Start");
            C caller = callerRef.get();
            if (caller != null) {
                observer.onChangedAsync(caller);
            }
            Log.d(LOG_TAG, "doInBackground: End");
        }
    }
}
