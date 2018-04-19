package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.activity.ActivityNavigator;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dialog that notifies the user that a battle is available to be fought.
 */
public final class BattleAvailableDialog {

    private static final String LOG_TAG = BattleAvailableDialog.class.getSimpleName();

    private BattleAvailableDialog() {
    }

    public static void show(Context context, long buildingId) {
        new LoadDialogAsyncTask(context, buildingId).execute();
    }

    /**
     * An {@link AsyncTask} that loads the battle information and then displays it to the user in
     * a dialog.
     */
    static class LoadDialogAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final long buildingId;

        private BuildingType buildingType;
        private UnitType enemyUnitType;
        private Map<Integer, UnitType> friendlyUnitTypes = new HashMap<>();
        private Map<Integer, Integer> friendlyUnitCountByType = new HashMap<>();
        private List<UnitWithType> friendlyUnitWithTypes;

        LoadDialogAsyncTask(Context context, long buildingId) {
            this.context = context;
            this.buildingId = buildingId;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // Load data to display.
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
            buildingType = buildingWithType.getBuildingType();
            enemyUnitType = dao.getUnitTypeById(buildingType.getEnemyUnitTypeId());

            if (enemyUnitType == null) {
                Log.e(LOG_TAG, "show: Something went wrong. The building doesn't have enemy " +
                        "units: " + buildingId + " - " + buildingType.getId());
                return null;
            }

            // Get the player's unit types.
            friendlyUnitWithTypes = dao.getUnitsWithTypeJoiningUser();
            for (UnitWithType unitWithType : friendlyUnitWithTypes) {
                int unitTypeId = unitWithType.getType().getId();
                if (!friendlyUnitTypes.containsKey(unitTypeId)) {
                    friendlyUnitTypes.put(unitTypeId, unitWithType.getType());

                    friendlyUnitCountByType.put(unitTypeId, 1);
                } else {
                    int amount = friendlyUnitCountByType.get(unitTypeId);
                    friendlyUnitCountByType.put(unitTypeId, amount + 1);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Detect if the user walked away from the building.
            final BuildingProximityDialogReceiver receiver =
                    new BuildingProximityDialogReceiver(context);

            // Create custom view.
            LayoutInflater inflater = LayoutInflater.from(context);
            View rootView = inflater.inflate(R.layout.dialog_battle_available, null);

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(rootView)
                    .setNegativeButton(
                            R.string.withdraw_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    RoosterBroadcastManager.getInstance(context)
                                            .unregister(receiver);
                                }
                            })
                    .setPositiveButton(
                            R.string.attack_button,
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

            buildUnitStatsTable(rootView);
        }

        private void buildUnitStatsTable(View rootView) {
            // Collect all unit types.
            List<UnitType> unitTypes = new ArrayList<>(friendlyUnitTypes.values());
            unitTypes.add(enemyUnitType);

            TableLayout tableLayout = rootView.findViewById(R.id.unit_stats_table_layout);
            TextView unitSummaryTextView = rootView.findViewById(R.id.unit_summary_text_view);

            unitSummaryTextView.setText(getUnitSummaryText());

            addingHeadingRow(tableLayout);

            for (UnitType unitType : unitTypes) {
                TableRow tableRow = new TableRow(context);
                tableLayout.addView(tableRow);

                addTableCell(tableRow, unitType.getName());
                addTableCell(tableRow, unitType.getAttack());
                addTableCell(tableRow, unitType.getDefense());
                addTableCell(tableRow, unitType.getDamage());
                addTableCell(tableRow, unitType.getArmor());
                addTableCell(tableRow, unitType.getHealth());
            }
        }

        private void addingHeadingRow(TableLayout tableLayout) {
            TableRow headingTableRow = new TableRow(context);
            tableLayout.addView(headingTableRow);

            TextView typeTextView = new TextView(context);
            typeTextView.setText(context.getString(R.string.unit_type_name_heading));
            typeTextView.setGravity(Gravity.START);
            headingTableRow.addView(typeTextView);

            addTableCell(headingTableRow, context.getString(R.string.attack_value_heading));
            addTableCell(headingTableRow, context.getString(R.string.defense_value_heading));
            addTableCell(headingTableRow, context.getString(R.string.damage_value_heading));
            addTableCell(headingTableRow, context.getString(R.string.armor_value_heading));
            addTableCell(headingTableRow, context.getString(R.string.health_value_heading));
        }

        private void addTableCell(TableRow tableRow, int value) {
            addTableCell(tableRow, Integer.toString(value));
        }

        private void addTableCell(TableRow tableRow, String text) {
            TextView typeTextView = new TextView(context);
            typeTextView.setText(text);
            typeTextView.setGravity(Gravity.END);
            tableRow.addView(typeTextView);
        }

        private String getUnitSummaryText() {
            StringBuilder sb = new StringBuilder();

            for (UnitType friendlyUnitType : friendlyUnitTypes.values()) {
                int friendlyUnitCount = friendlyUnitCountByType.get(friendlyUnitType.getId());
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(friendlyUnitCount);
                sb.append(" ");
                sb.append(friendlyUnitType.getName());
            }

            sb.append(" vs ");
            sb.append(buildingType.getEnemyUnitCount());
            sb.append(" ");
            sb.append(enemyUnitType.getName());

            return sb.toString();
        }
    }
}
