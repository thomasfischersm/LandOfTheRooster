package com.playposse.landoftherooster.dialog.support;

import android.app.DialogFragment;
import android.util.Log;

import com.playposse.landoftherooster.services.broadcastintent.LeftBuildingBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nullable;

/**
 * A {@link RoosterBroadcastManager.RoosterBroadcastReceiver} that makes the dialog disappear
 * if the user walks away from the building.
 */
public class BuildingProximityDialogReceiver implements RoosterBroadcastManager.RoosterBroadcastReceiver {

    private static final String LOG_TAG = BuildingProximityDialogReceiver.class.getSimpleName();

    private final DialogFragment dialogFragment;

    @Nullable
    private ScheduledExecutorService scheduledExecutorService;

    public BuildingProximityDialogReceiver(DialogFragment dialogFragment) {
        this.dialogFragment = dialogFragment;

        RoosterBroadcastManager.getInstance(dialogFragment.getActivity())
                .register(this);
    }

    @Override
    public void onReceive(RoosterBroadcastIntent roosterIntent) {
        if (roosterIntent instanceof LeftBuildingBroadcastIntent) {
            Log.i(LOG_TAG, "onReceive: Automatically dismissing dialog because the user " +
                    "walked away from the building.");

            dialogFragment.dismiss();
        }
    }
}
