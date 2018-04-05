package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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

    public static void show(Context context, int buildingId) {
        new LoadDialogAsyncTask(context, buildingId).execute();
    }

    /**
     * An {@link AsyncTask} that loads the battle information and then displays it to the user in
     * a dialog.
     */
    static class LoadDialogAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final int buildingId;

        private BuildingType buildingType;
        private UnitType unitType;

        LoadDialogAsyncTask(Context context, int buildingId) {
            this.context = context;
            this.buildingId = buildingId;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // Load data to display.
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
            buildingType = buildingWithType.getBuildingType();
            unitType = dao.getUnitTypeById(buildingType.getEnemyUnitTypeId());

            if (unitType == null) {
                Log.e(LOG_TAG, "show: Something went wrong. The building doesn't have enemy " +
                        "units: " + buildingId + " - " + buildingType.getId());
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Detect if the user walked away from the building.
            final BuildingProximityDialogReceiver receiver =
                    new BuildingProximityDialogReceiver(context);

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
}
