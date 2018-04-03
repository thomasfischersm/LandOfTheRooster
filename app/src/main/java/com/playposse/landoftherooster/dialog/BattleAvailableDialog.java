package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.activity.ActivityNavigator;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

/**
 * A dialog that notifies the user that a battle is available to be fought.
 */
public final class BattleAvailableDialog {

    private static final String LOG_TAG = BattleAvailableDialog.class.getSimpleName();

    private BattleAvailableDialog() {
    }

    public static void show(final Context context, final int buildingId) {
        // Detect if the user walked away from the building.
        final BuildingProximityDialogReceiver receiver =
                new BuildingProximityDialogReceiver(context);

        // Load data to display.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        BuildingType buildingType = buildingWithType.getBuildingType();
        UnitType unitType = dao.getUnitTypeById(buildingType.getEnemyUnitTypeId());

        if (unitType == null) {
            Log.e(LOG_TAG, "show: Something went wrong. The building doesn't have enemy " +
                    "units: " + buildingId + " - " + buildingType.getId());
            return;
        }

        String msg = context.getString(
                R.string.battle_available_dialog_msg,
                buildingType.getName(),
                buildingType.getEnemyUnitCount(),
                unitType.getName());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.battle_available_dialog_title)
                .setMessage(msg)
                .setNegativeButton(
                        R.string.dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                RoosterBroadcastManager.getInstance(context)
                                        .unregister(receiver);
                            }
                        })
                .setPositiveButton(
                        R.string.dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                RoosterBroadcastManager.getInstance(context)
                                        .unregister(receiver);

                                ActivityNavigator.startBattleActivity(context, buildingId);
                            }
                        })
                .show();

        receiver.setDialog(dialog);
    }
}
