package com.playposse.landoftherooster.util;

/**
 * A {@link Runnable} that can be canceled.
 */
public abstract class CancelableRunnable implements Runnable {

    private boolean isCanceled = false;

    @Override
    public final void run() {
        if (!isCanceled) {
            maybeRun();
        }
    }

    protected abstract void maybeRun();

    public void cancel() {
        isCanceled = true;
    }
}
