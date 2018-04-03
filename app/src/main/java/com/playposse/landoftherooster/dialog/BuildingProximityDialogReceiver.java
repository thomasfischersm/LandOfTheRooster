package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.playposse.landoftherooster.services.broadcastintent.LeftBuildingBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

/**
 * A {@link RoosterBroadcastManager.RoosterBroadcastReceiver} that makes the dialog disappear
 * if the user walks away from the building.
 */
class BuildingProximityDialogReceiver implements RoosterBroadcastManager.RoosterBroadcastReceiver {

    private static final String LOG_TAG = BuildingProximityDialogReceiver.class.getSimpleName();

    private AlertDialog dialog;

    BuildingProximityDialogReceiver(Context context) {
        RoosterBroadcastManager.getInstance(context)
                .register(this);
    }

    @Override
    public void onReceive(RoosterBroadcastIntent roosterIntent) {
        if (roosterIntent instanceof LeftBuildingBroadcastIntent) {
            Log.i(LOG_TAG, "onReceive: Automatically dismissing dialog because the user " +
                    "walked away from the building.");

            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }
}
