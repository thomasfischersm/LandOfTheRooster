package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.dialog.support.BuildingProximityDialogReceiver;
import com.playposse.landoftherooster.dialog.support.CountdownUpdateRunnable;
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
public abstract class BaseDialogFragment extends DialogFragment {

    private static final String LOG_TAG = BaseDialogFragment.class.getSimpleName();

    private final int layoutResId;

    private boolean isInitialized = false;
    private boolean disappearOnDistance = false;
    @Nullable private ButtonInfo positiveButtonInfo;
    @Nullable private ButtonInfo negativeButtonInfo;

    private View rootView;
    @Nullable private BuildingProximityDialogReceiver proximityReceiver;
    @Nullable private ScheduledExecutorService scheduledExecutorService;
    @Nullable private CountdownUpdateRunnable countdownTask;

    protected BaseDialogFragment(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isInitialized = true;

        readArguments(savedInstanceState);

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        rootView = inflater.inflate(layoutResId, null);

        // Build default dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(rootView);

        // Add positive button.
        if (positiveButtonInfo != null) {
            builder.setPositiveButton(
                    positiveButtonInfo.buttonLabelResId,
                    positiveButtonInfo.getOnClickListener());
        }

        // Add negative button.
        if (negativeButtonInfo != null) {
            builder.setNegativeButton(
                    negativeButtonInfo.buttonLabelResId,
                    negativeButtonInfo.getOnClickListener());
        }

        AlertDialog dialog = builder.create();

        // Make the dialog disappear when the user walks away.
        if (disappearOnDistance) {
            proximityReceiver = new BuildingProximityDialogReceiver(this);
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

    protected abstract void readArguments(Bundle savedInstanceState);

    protected abstract void doInBackground();

    protected abstract void onPostExecute();

    protected void onCountdownComplete() {
    }

    private void failOnInitialized() {
        if (isInitialized) {
            throw new IllegalStateException(
                    "Cannot configure BaseDialogFragment after initialization.");
        }
    }

    protected void setShowReturnToMapButton(boolean showReturnToMapButton) {
        failOnInitialized();

        // Add positive action to return to map.
        if (showReturnToMapButton) {
            positiveButtonInfo = new ButtonInfo(
                    R.string.return_to_map_button_label,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    });
        } else {
            positiveButtonInfo = null;
        }
    }

    protected void setDisappearOnDistance(boolean disappearOnDistance) {
        failOnInitialized();
        this.disappearOnDistance = disappearOnDistance;
    }

    protected void setPositiveButton(
            int buttonLabelResId,
            DialogInterface.OnClickListener clickListener) {

        positiveButtonInfo = new ButtonInfo(buttonLabelResId, clickListener);
    }

    protected void setNegativeButton(
            int buttonLabelResId,
            DialogInterface.OnClickListener clickListener) {

        negativeButtonInfo = new ButtonInfo(buttonLabelResId, clickListener);
    }

    protected void startCountdown(
            @Nullable TextView headingTextView,
            TextView countdownTextView,
            long remainingMs) {

        closeCountdown();

        setCountdownVisibility(headingTextView, countdownTextView, View.VISIBLE);

        Runnable countdownCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                closeCountdown();
                onCountdownComplete();
            }
        };

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        countdownTask = new CountdownUpdateRunnable(
                countdownTextView,
                remainingMs,
                countdownCompleteRunnable);
        scheduledExecutorService.scheduleAtFixedRate(countdownTask, 1, 1, TimeUnit.SECONDS);
    }

    protected void clearCountdown(@Nullable TextView headingTextView, TextView countdownTextView) {
        closeCountdown();

        setCountdownVisibility(headingTextView, countdownTextView, View.GONE);
    }

    private void setCountdownVisibility(
            @Nullable TextView headingTextView,
            TextView countdownTextView, int gone) {

        if (headingTextView != null) {
            headingTextView.setVisibility(gone);
        }
        countdownTextView.setVisibility(gone);
    }

    private void closeCountdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    protected long getCountdownRemainingMs() {
        if (countdownTask != null) {
            return countdownTask.getRemainingMs();
        } else {
            return -1;
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

        private final WeakReference<BaseDialogFragment> fragmentReference;
        @Nullable private final Runnable runnable;

        private LoadAsyncTask(BaseDialogFragment fragment, @Nullable Runnable runnable) {
            this.runnable = runnable;

            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "doInBackground: Start loading dialog");
            BaseDialogFragment fragment = fragmentReference.get();
            if ((fragment != null) && (runnable != null)) {
                runnable.run();
            }

            fragment = fragmentReference.get();
            if (fragment != null) {
                fragment.doInBackground();
            }
            Log.d(LOG_TAG, "doInBackground: Finished loading dialog");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(LOG_TAG, "onPostExecute: Start populating dialog.");
            BaseDialogFragment fragment = fragmentReference.get();
            if (fragment != null) {
                ButterKnife.bind(fragment, fragment.rootView);

                fragment.onPostExecute();
            }
            Log.d(LOG_TAG, "onPostExecute: Finished populating dialog.");
        }
    }

    /**
     * A little helper to keep information about optional positive and negative dialog buttons
     * together.
     */
    private static class ButtonInfo {

        private final int buttonLabelResId;
        private final DialogInterface.OnClickListener onClickListener;

        private ButtonInfo(int buttonLabelResId, DialogInterface.OnClickListener onClickListener) {
            this.buttonLabelResId = buttonLabelResId;
            this.onClickListener = onClickListener;
        }

        private int getButtonLabelResId() {
            return buttonLabelResId;
        }

        private DialogInterface.OnClickListener getOnClickListener() {
            return onClickListener;
        }
    }
}
