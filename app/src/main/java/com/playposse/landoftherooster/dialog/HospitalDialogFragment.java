package com.playposse.landoftherooster.dialog;

import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.util.GlideUtil;
import com.playposse.landoftherooster.util.GridLayoutRowViewHolder;
import com.playposse.landoftherooster.util.MutableLong;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * A dialog that allows the user to drop off and pickup wounded/recovered units at a hospital.
 */
public class HospitalDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = HospitalDialogFragment.class.getSimpleName();

    private static final String BUILDING_ID_ARG = "buildingId";

    private long buildingId;

    private BuildingWithType buildingWithType;
    private List<UnitWithType> woundedUnitWithTypes;
    private List<UnitWithType> hospitalizedUnitWithTypes;
    private List<UnitWithType> recoveredUnitWithTypes;
    private int peasantCountInBuilding;
    private int peasantCountJoiningUser;

    @BindView(R.id.building_name_text_view) TextView buildingNameTextView;
    @BindView(R.id.building_icon_image_view) ImageView buildingIconImageView;
    @BindView(R.id.wounded_heading_text_view) TextView woundedHeadingTextView;
    @BindView(R.id.wounded_grid_layout) GridLayout woundedGridLayout;
    @BindView(R.id.hospitalized_heading_text_view) TextView hospitalizedHeadingTextView;
    @BindView(R.id.hospitalized_grid_layout) GridLayout hospitalizedGridLayout;
    @BindView(R.id.recovered_heading_text_view) TextView recoveredHeadingTextView;
    @BindView(R.id.recovered_grid_layout) GridLayout recoveredGridLayout;
    @BindView(R.id.peasant_count_text_view) TextView peasantCountTextView;
    @BindView(R.id.assign_peasant_button) Button assignPeasantButton;

    public HospitalDialogFragment() {
        super(R.layout.dialog_hospital);

        setDisappearOnDistance(true);
        setShowReturnToMapButton(true);
        setReloadBusinessEvents(Arrays.asList(
                PostAdmitUnitToHospitalEvent.class,
                PostPickUpUnitFromHospitalEvent.class,
                PostAssignPeasantEvent.class,
                PostCompleteHealingEvent.class));
    }

    public static HospitalDialogFragment newInstance(long buildingId) {
        Log.d(LOG_TAG, "newInstance: Start newInstance");
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);

        HospitalDialogFragment fragment = new HospitalDialogFragment();
        fragment.setArguments(args);

        Log.d(LOG_TAG, "newInstance: End newInstance");
        return fragment;
    }

    @Override
    protected Long readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);
        return buildingId;
    }

    @Override
    protected void doInBackground() {
        Log.d(LOG_TAG, "doInBackground: Start doInBackground");
        RoosterDao dao = RoosterDatabase.getInstance(getActivity()).getDao();

        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        peasantCountInBuilding = GameConfig.IMPLIED_PEASANT_COUNT;
        peasantCountJoiningUser = dao.getUnitCountJoiningUser(GameConfig.PEASANT_ID);
        woundedUnitWithTypes = dao.getWoundedUnitsWithTypeJoiningUser();

        // Sort hospitalized, recovered units, and peasants.
        List<UnitWithType> unitsWithTypes = dao.getUnitsWithTypeByBuildingId(buildingId);
        hospitalizedUnitWithTypes = new ArrayList<>();
        recoveredUnitWithTypes = new ArrayList<>();
        for (UnitWithType unitWithType : unitsWithTypes) {
            if (unitWithType.isInjured()) {
                hospitalizedUnitWithTypes.add(unitWithType);
            } else {
                recoveredUnitWithTypes.add(unitWithType);
                if (unitWithType.getType().getId() == GameConfig.PEASANT_ID) {
                    peasantCountInBuilding++;
                    peasantCountInBuilding = Math.min(
                            peasantCountInBuilding,
                            GameConfig.MAX_PEASANT_BUILDING_CAPACITY);
                }
            }
        }
        Log.d(LOG_TAG, "doInBackground: End doInBackground");
    }

    @Override
    protected void onPostExecute() {
        Log.d(LOG_TAG, "onPostExecute: Starting to populate.");

        // Populate header.
        String buildingName = StringUtil.capitalize(buildingWithType.getBuildingType().getName());
        buildingNameTextView.setText(buildingName);
        GlideUtil.loadBuildingIcon(buildingIconImageView, buildingWithType);
        Log.d(LOG_TAG, "onPostExecute: 1");

        populateWoundedSection();
        Log.d(LOG_TAG, "onPostExecute: 2");
        populateHospitalizedSection();
        Log.d(LOG_TAG, "onPostExecute: 3");
        populateRecoveredSection();
        Log.d(LOG_TAG, "onPostExecute: 4");
        populatePeasantSection();
        Log.d(LOG_TAG, "onPostExecute: Finished populating");
    }

    private void populateWoundedSection() {
        // Hide empty section.
        if (woundedUnitWithTypes.size() == 0) {
            woundedHeadingTextView.setVisibility(View.GONE);
            woundedGridLayout.setVisibility(View.GONE);
            return;
        }

        // Show empty section.
        woundedHeadingTextView.setVisibility(View.VISIBLE);
        woundedGridLayout.setVisibility(View.VISIBLE);
        woundedGridLayout.removeAllViews();

        for (final UnitWithType unitWithType : woundedUnitWithTypes) {
            new WoundedRowViewHolder().apply(unitWithType);
        }
    }

    private void populateHospitalizedSection() {
        // Hide empty section.
        if (hospitalizedUnitWithTypes.size() == 0) {
            hospitalizedHeadingTextView.setVisibility(View.GONE);
            hospitalizedGridLayout.setVisibility(View.GONE);
            return;
        }

        // Show empty section.
        hospitalizedHeadingTextView.setVisibility(View.VISIBLE);
        hospitalizedGridLayout.setVisibility(View.VISIBLE);
        hospitalizedGridLayout.removeAllViews();

        final MutableLong healingDurationSum = new MutableLong(0);// TODO Subtract already passed building healing start
        for (final UnitWithType unitWithType : hospitalizedUnitWithTypes) {
            new HospitalizedGridLayoutRowViewHolder(healingDurationSum).apply(unitWithType);
        }
    }


    private void populateRecoveredSection() {
        // Hide empty section.
        if (recoveredUnitWithTypes.size() == 0) {
            recoveredHeadingTextView.setVisibility(View.GONE);
            recoveredGridLayout.setVisibility(View.GONE);
            return;
        }

        // Show empty section.
        Log.d(LOG_TAG, "populateRecoveredSection: a");
        recoveredHeadingTextView.setVisibility(View.VISIBLE);
        Log.d(LOG_TAG, "populateRecoveredSection: b");
        recoveredGridLayout.setVisibility(View.VISIBLE);
        Log.d(LOG_TAG, "populateRecoveredSection: c");
        recoveredGridLayout.removeAllViews();
        Log.d(LOG_TAG, "populateRecoveredSection: e");

        for (final UnitWithType unitWithType : recoveredUnitWithTypes) {
            Log.d(LOG_TAG, "populateRecoveredSection: f");
            new RecoveredRowViewHolder().apply(unitWithType);
            Log.d(LOG_TAG, "populateRecoveredSection: i");
        }
    }

    private void populatePeasantSection() {
        String peasantCountStr = getString(
                R.string.peasant_count,
                peasantCountInBuilding,
                GameConfig.MAX_PEASANT_BUILDING_CAPACITY);
        peasantCountTextView.setText(peasantCountStr);

        if ((peasantCountInBuilding == GameConfig.MAX_PEASANT_BUILDING_CAPACITY)
                || (peasantCountJoiningUser == 0)) {
            assignPeasantButton.setEnabled(false);
        } else {
            assignPeasantButton.setEnabled(!isRemote());
            assignPeasantButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAssignPeasantClicked();
                }
            });
        }
    }

    private void onAssignPeasantClicked() {
        reload(new Runnable() {
            @Override
            public void run() {
                BusinessEngine.get()
                        .triggerEvent(new AssignPeasantEvent(buildingId));
            }
        });
    }

    private void onAdmitWoundedUnitClicked(final UnitWithType unitWithType) {
        reload(new Runnable() {
            @Override
            public void run() {
                long unitId = unitWithType.getUnit().getId();
                BusinessEngine.get()
                        .triggerEvent(new AdmitUnitToHospitalEvent(buildingId, unitId));
            }
        });
    }

    private void onPickUpRecoveredUnitClicked(final UnitWithType unitWithType) {
        reload(new Runnable() {
            @Override
            public void run() {
                long unitId = unitWithType.getUnit().getId();
                BusinessEngine.get()
                        .triggerEvent(new PickUpUnitFromHospitalEvent(buildingId, unitId));
            }
        });
    }

    private static long computeHealingDuration(
            UnitWithType firstSickUnitWithType,
            int peasantCount) {

        // Calculate healing time.
        return firstSickUnitWithType.getHealingTimeMs(peasantCount);
    }

    class WoundedRowViewHolder extends GridLayoutRowViewHolder<UnitWithType> {

        @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
        @BindView(R.id.health_text_view) TextView healthTextView;
        @BindView(R.id.admit_button) Button admitButton;

        private WoundedRowViewHolder() {
            super(HospitalDialogFragment.this.woundedGridLayout, R.layout.list_item_wounded_unit);
        }

        @Override
        protected void populate(final UnitWithType unitWithType) {
            String healthStr = getString(
                    R.string.wounded_health,
                    unitWithType.getUnit().getHealth(),
                    unitWithType.getType().getHealth());

            unitTypeNameTextView.setText(unitWithType.getType().getName());
            healthTextView.setText(healthStr);

            admitButton.setEnabled(!isRemote());
            admitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAdmitWoundedUnitClicked(unitWithType);
                }
            });
        }
    }

    class HospitalizedGridLayoutRowViewHolder extends GridLayoutRowViewHolder<UnitWithType> {

        private final MutableLong healingDurationSum;
        @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
        @BindView(R.id.health_text_view) TextView healthTextView;
        @BindView(R.id.estimated_time_text_view) TextView estimatedTimeTextView;

        private HospitalizedGridLayoutRowViewHolder(MutableLong healingDurationSum) {
            super(
                    HospitalDialogFragment.this.hospitalizedGridLayout,
                    R.layout.list_item_hospitalized_unit);

            this.healingDurationSum = healingDurationSum;
        }

        @Override
        protected void populate(UnitWithType unitWithType) {
            // @BindView is not working because ButterKnife can't bind the same view twice.
//                    unitTypeNameTextView =
//                            hospitalizedGridLayout.findViewById(R.id.unit_type_name_text_view);
//                    healthTextView = hospitalizedGridLayout.findViewById(R.id.health_text_view);
//                    estimatedTimeTextView =
//                            hospitalizedGridLayout.findViewById(R.id.estimated_time_text_view);

            String healthStr = getString(
                    R.string.wounded_health,
                    unitWithType.getUnit().getHealth(),
                    unitWithType.getType().getHealth());

            long healingDuration = computeHealingDuration(
                    unitWithType,
                    peasantCountInBuilding);
            healingDurationSum.add(healingDuration);
            long durationMin = healingDurationSum.getValue() / 60 / 1_000;
            String estimateStr =
                    getString(R.string.healing_estimate, durationMin);

            unitTypeNameTextView.setText(unitWithType.getType().getName());
            healthTextView.setText(healthStr);
            estimatedTimeTextView.setText(estimateStr);
        }
    }

    class RecoveredRowViewHolder extends GridLayoutRowViewHolder<UnitWithType> {

        @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
        @BindView(R.id.pick_up_button) Button pickUpButton;

        private RecoveredRowViewHolder() {
            super(HospitalDialogFragment.this.recoveredGridLayout, R.layout.list_item_recovered_unit);
        }

        @Override
        protected void populate(final UnitWithType unitWithType) {
            // @BindView is not working because ButterKnife can't bind the same view twice.
//                    unitTypeNameTextView =
//                            recoveredGridLayout.findViewById(R.id.unit_type_name_text_view);
//                    pickUpButton = recoveredGridLayout.findViewById(R.id.pick_up_button);
            Log.d(LOG_TAG, "populate: g");

            unitTypeNameTextView.setText(unitWithType.getType().getName());

            pickUpButton.setEnabled(!isRemote());
            pickUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPickUpRecoveredUnitClicked(unitWithType);
                }
            });
            Log.d(LOG_TAG, "populate: h");
        }
    }
}
