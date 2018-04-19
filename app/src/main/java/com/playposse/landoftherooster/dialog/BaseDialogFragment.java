package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import butterknife.ButterKnife;

/**
 * A wrapper that makes it easier to deal with {@link AlertDialog.Builder}. The dialogs of this
 * app commonly have a custom view with
 */
public abstract class BaseDialogFragment<D extends BaseDialogFragment<?>> extends DialogFragment {

    private final int layoutResId;

    private boolean isInitialized = false;
    private boolean showReturnToMapButton = false;
    private boolean disappearOnDistance = false;

    private View rootView;
    private BuildingProximityDialogReceiver proximityReceiver;
    private ScheduledExecutorService scheduledExecutorService;

    protected BaseDialogFragment(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isInitialized = true;

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        rootView = inflater.inflate(layoutResId, null);

        // Build default dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(rootView);

        // Add positive action to return to map.
        if (showReturnToMapButton) {
            builder
                    .setPositiveButton(
                            R.string.return_to_map_button_label,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dismiss();
                                }
                            });
        }
        AlertDialog dialog = builder.create();

        // Make the dialog disappear when the user walks away.
        if (disappearOnDistance) {
            proximityReceiver = new BuildingProximityDialogReceiver(getActivity());
            proximityReceiver.setDialog(dialog);
        }

        new LoadAsyncTask(this, null).execute();

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (proximityReceiver != null) {
            RoosterBroadcastManager.getInstance(getActivity())
                    .unregister(proximityReceiver);
            proximityReceiver = null;
        }

        closeCountdown();
    }

    protected abstract void doInBackground();

    protected abstract void onPostExecute();

    protected void onCountdownAtZero() {}

    @SuppressWarnings("unchecked")
    private D get() {
        return (D) this;
    }

    private void failOnInitialized() {
        if (isInitialized) {
            throw new IllegalStateException("Cannot configure BaseDialogFragment after initialization.");
        }
    }

    public D setShowReturnToMapButton(boolean showReturnToMapButton) {
        failOnInitialized();
        this.showReturnToMapButton = showReturnToMapButton;
        return get();
    }

    public D setDisappearOnDistance(boolean disappearOnDistance) {
        failOnInitialized();
        this.disappearOnDistance = disappearOnDistance;
        return get();
    }

    protected void startCountdown(
            TextView headingTextView,
            TextView countdownTextView,
            long remainingMs) {

        closeCountdown();

        headingTextView.setVisibility(View.VISIBLE);
        countdownTextView.setVisibility(View.VISIBLE);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        CountdownUpdateRunnable countdownTask =
                new CountdownUpdateRunnable(countdownTextView, remainingMs);
        scheduledExecutorService.scheduleAtFixedRate(countdownTask, 1, 1, TimeUnit.SECONDS);
    }

    protected void clearCountdown(TextView headingTextView, TextView countdownTextView) {
        closeCountdown();

        headingTextView.setVisibility(View.GONE);
        countdownTextView.setVisibility(View.GONE);
    }

    private void closeCountdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    protected void reload(@Nullable Runnable runnable) {
        new LoadAsyncTask(this, runnable).execute();
    }

    /**
     * An {@link AsyncTask} to power asynchronous loading of data. The actual dialog class has
     * methods that should be overridden by implementing classes.
     */
    private static class LoadAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<BaseDialogFragment<?>> fragmentReference;
        @Nullable private final Runnable runnable;

        private LoadAsyncTask(BaseDialogFragment<?> fragment, @Nullable Runnable runnable) {
            this.runnable = runnable;

            fragmentReference = new WeakReference<BaseDialogFragment<?>>(fragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BaseDialogFragment<?> fragment = fragmentReference.get();
            if ((fragment != null) && (runnable != null)) {
                runnable.run();
            }

            fragment = fragmentReference.get();
            if (fragment != null) {
                fragment.doInBackground();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            BaseDialogFragment<?> fragment = fragmentReference.get();
            if (fragment != null) {
                ButterKnife.bind(fragment, fragment.rootView);

                fragment.onPostExecute();
            }
        }
    }
}
