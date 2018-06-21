package com.playposse.landoftherooster.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingTypeRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostRespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * A dialog that notifies the user that a battle is available to be fought.
 */
public class BattleAvailableDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = BattleAvailableDialogFragment.class.getSimpleName();

    private static final String BUILDING_ID_ARG = "buildingId";

    private long buildingId;
    private BuildingType buildingType;
    private UnitType enemyUnitType;
    private Map<Long, UnitType> friendlyUnitTypes = new HashMap<>();
    private Map<Long, Integer> friendlyUnitCountByType = new HashMap<>();
    private List<UnitWithType> friendlyUnitWithTypes;

    @BindView(R.id.unit_stats_table_layout) TableLayout unitStatsTableLayout;
    @BindView(R.id.unit_summary_text_view) TextView unitSummaryTextView;


    public BattleAvailableDialogFragment() {
        super(R.layout.dialog_battle_available);

        setShowReturnToMapButton(true);
        setDisappearOnDistance(true);
        setReloadBusinessEvents(Arrays.asList(
                PostRespawnBattleBuildingEvent.class,
                PostBattleEvent.class));

        setNegativeButton(
                R.string.withdraw_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        setPositiveButton(
                R.string.attack_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();

                        // TODO: If the user is remote, disable the button.
                        if (!isRemote()) {
                            BattleDialogFragment.newInstance(buildingId)
                                    .show(getFragmentManager(), null);
                        }
                    }
                });
    }

    public static BattleAvailableDialogFragment newInstance(long buildingId) {
        BattleAvailableDialogFragment fragment = new BattleAvailableDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Long readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);
        return buildingId;
    }

    @Override
    protected void doInBackground(Context appContext) {
        // Load data to display.
        RoosterDao dao = RoosterDatabase.getInstance(getActivity()).getDao();
        BuildingWithType buildingWithType =
                BuildingTypeRepository.get(dao).queryBuildingWithType(buildingId);
        buildingType = buildingWithType.getBuildingType();
        enemyUnitType = dao.getUnitTypeById(buildingType.getEnemyUnitTypeId());

        if (enemyUnitType == null) {
            Log.e(LOG_TAG, "show: Something went wrong. The building doesn't have enemy " +
                    "units: " + buildingId + " - " + buildingType.getId());
            return;
        }

        // Get the player's unit types.
        friendlyUnitWithTypes = dao.getUnitsWithTypeJoiningUser();
        for (UnitWithType unitWithType : friendlyUnitWithTypes) {
            long unitTypeId = unitWithType.getType().getId();
            if (!friendlyUnitTypes.containsKey(unitTypeId)) {
                friendlyUnitTypes.put(unitTypeId, unitWithType.getType());

                friendlyUnitCountByType.put(unitTypeId, 1);
            } else {
                int amount = friendlyUnitCountByType.get(unitTypeId);
                friendlyUnitCountByType.put(unitTypeId, amount + 1);
            }
        }
    }

    @Override
    protected void onPostExecute() {
        // Collect all unit types.
        List<UnitType> unitTypes = new ArrayList<>(friendlyUnitTypes.values());
        unitTypes.add(enemyUnitType);

        unitSummaryTextView.setText(getUnitSummaryText());

        addingHeadingRow(unitStatsTableLayout);

        for (UnitType unitType : unitTypes) {
            TableRow tableRow = new TableRow(getActivity());
            unitStatsTableLayout.addView(tableRow);

            addTableCell(tableRow, unitType.getName());
            addTableCell(tableRow, unitType.getAttack());
            addTableCell(tableRow, unitType.getDefense());
            addTableCell(tableRow, unitType.getDamage());
            addTableCell(tableRow, unitType.getArmor());
            addTableCell(tableRow, unitType.getHealth());
        }
    }

    private void addingHeadingRow(TableLayout tableLayout) {
        TableRow headingTableRow = new TableRow(getActivity());
        tableLayout.addView(headingTableRow);

        TextView typeTextView = new TextView(getActivity());
        typeTextView.setText(getString(R.string.unit_type_name_heading));
        typeTextView.setGravity(Gravity.START);
        headingTableRow.addView(typeTextView);

        addTableCell(headingTableRow, getString(R.string.attack_value_heading));
        addTableCell(headingTableRow, getString(R.string.defense_value_heading));
        addTableCell(headingTableRow, getString(R.string.damage_value_heading));
        addTableCell(headingTableRow, getString(R.string.armor_value_heading));
        addTableCell(headingTableRow, getString(R.string.health_value_heading));
    }

    private void addTableCell(TableRow tableRow, int value) {
        addTableCell(tableRow, Integer.toString(value));
    }

    private void addTableCell(TableRow tableRow, String text) {
        TextView typeTextView = new TextView(getActivity());
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
