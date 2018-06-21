package com.playposse.landoftherooster.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessEventListener;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingZoneRepository;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneEnteredEvent;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneExitedEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.dialog.support.CountdownUpdateRunnable;
import com.playposse.landoftherooster.dialog.support.ExitBuildingZoneListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
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
    private Long buildingId;
    private boolean isRemote;
    private boolean disappearOnDistance = false;
    @Nullable private ButtonInfo positiveButtonInfo;
    @Nullable private ButtonInfo negativeButtonInfo;
    private RoosterDao dao;

    private View rootView;

    @Nullable private ExitBuildingZoneListener exitBuildingZoneListener;
    @Nullable private EnterBuildingZoneListener enterBuildingZoneListener;
    @Nullable private ScheduledExecutorService scheduledExecutorService;
    @Nullable private CountdownUpdateRunnable countdownTask;
    @Nullable private List<Class<? extends BusinessEvent>> reloadBusinessEvents;
    @Nullable private ReloadBusinessEventListener reloadBusinessEventListener;

    protected BaseDialogFragment(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isInitialized = true;

        dao = RoosterDatabase.getInstance(getActivity()).getDao();
        buildingId = readArguments(savedInstanceState);
        Long currentBuildingId = BuildingZoneRepository.get(dao).getCurrentBuildingId();
        isRemote = !Objects.equals(buildingId, currentBuildingId);

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
            exitBuildingZoneListener = new ExitBuildingZoneListener(this);
            BusinessEngine.get()
                    .addEventListener(BuildingZoneExitedEvent.class, exitBuildingZoneListener);
        }

        // Add listener to switch dialog if the user walks into a building zone.
        enterBuildingZoneListener = new EnterBuildingZoneListener();
        BusinessEngine.get()
                .addEventListener(BuildingZoneEnteredEvent.class, enterBuildingZoneListener);

        updateRemoteIndications();

        new LoadAsyncTask(this, null).execute();

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Unregister listener to exit building zone.
        if (exitBuildingZoneListener != null) {
            BusinessEngine.get()
                    .removeEventListener(BuildingZoneExitedEvent.class, exitBuildingZoneListener);
            exitBuildingZoneListener = null;
        }

        // Unregister listener to enter building zone.
        if (enterBuildingZoneListener != null) {
            BusinessEngine.get()
                    .removeEventListener(BuildingZoneEnteredEvent.class, enterBuildingZoneListener);
        }

        // Unregister listener for reload events.
        if (reloadBusinessEvents != null) {
            for (Class<? extends BusinessEvent> eventClass : reloadBusinessEvents) {
                BusinessEngine.get()
                        .removeEventListener(eventClass, reloadBusinessEventListener);
            }

            reloadBusinessEvents = null;
        }

        closeCountdown();
    }

    /**
     * Let's the implementing dialog read arguments. The method is expected to return the building
     * id.
     */
    protected abstract Long readArguments(Bundle savedInstanceState);

    /**
     * Let's the fragment execute heavy work on a background thread. The appContext is passed, so
     * that the implementing code won't throw a NPE when trying to access a disconnected activity.
     * @param appContext
     */
    protected abstract void doInBackground(Context appContext);

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

    /**
     * Updates visual indicators in the dialog, so that the user knows if the building is local
     * and can be interacted with.
     */
    protected void updateRemoteIndications() {
        TextView remoteMessageTextView = rootView.findViewById(R.id.remote_message_text_view);

        if (isRemote) {
            // Update for remote.
            rootView.setBackgroundColor(getResources().getColor(R.color.remoteDialogBgColor));
            if (remoteMessageTextView != null) {
                remoteMessageTextView.setVisibility(View.VISIBLE);
            }
        } else {
            // Update for local.
            rootView.setBackgroundColor(getResources().getColor(R.color.localDialogBgColor));
            if (remoteMessageTextView != null) {
                remoteMessageTextView.setVisibility(View.GONE);
            }
        }
    }

    protected void setReloadBusinessEvents(List<Class<? extends BusinessEvent>> businessEvents) {
        reloadBusinessEvents = businessEvents;

        reloadBusinessEventListener = new ReloadBusinessEventListener();
        if (businessEvents != null) {
            for (Class<? extends BusinessEvent> eventClass : businessEvents) {
                BusinessEngine.get()
                        .addEventListener(eventClass, reloadBusinessEventListener);
            }
        }
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

    protected boolean isRemote() {
        return isRemote;
    }

    public RoosterDao getDao() {
        return dao;
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
                Context context = fragment.getActivity();
                if (context != null) {
                    fragment.doInBackground(context.getApplicationContext());
                }
            }
            Log.d(LOG_TAG, "doInBackground: Finished loading dialog");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(LOG_TAG, "onPostExecute: Start populating dialog.");

            // If the fragment or activity are null, the activity has been disconnected.
            BaseDialogFragment fragment = fragmentReference.get();
            if ((fragment != null) && (fragment.getActivity() != null)) {
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

    /**
     * A {@link BusinessEventListener} that reload the current dialog if the buildingId matches.
     */
    private class ReloadBusinessEventListener implements BusinessEventListener {

        @Override
        public void onEvent(BusinessEvent event, BusinessDataCache cache) {
            if ((buildingId != null) && (buildingId.equals(event.getBuildingId()))) {
                reload(null);
            }
        }
    }

    /**
     * A {@link BusinessEventListener} that listens to the user walking into a building zone. If the
     * user does, it checks if the dialog should switch from remote to local mode.
     */
    private class EnterBuildingZoneListener implements  BusinessEventListener {

        @Override
        public void onEvent(BusinessEvent event, BusinessDataCache cache) {
            Long currentBuildingId = BuildingZoneRepository.get(dao).getCurrentBuildingId();
            boolean newIsRemote = !Objects.equals(buildingId, currentBuildingId);

            if (!isRemote && newIsRemote) {
                // Reload the dialog in local mode.
                isRemote = true;

                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateRemoteIndications();
                        }
                    });
                }

                reload(null);
            }
        }
    }
}
