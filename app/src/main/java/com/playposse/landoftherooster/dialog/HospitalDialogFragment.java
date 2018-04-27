package com.playposse.landoftherooster.dialog;

import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.services.broadcastintent.HospitalAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;
import com.playposse.landoftherooster.services.time.HospitalService;
import com.playposse.landoftherooster.util.GlideUtil;
import com.playposse.landoftherooster.util.GridLayoutRowViewHolder;
import com.playposse.landoftherooster.util.MutableLong;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A dialog that allows the user to drop off and pickup wounded/recovered units at a hospital.
 */
public class HospitalDialogFragment extends BaseDialogFragment {

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
    }

    public static HospitalDialogFragment newInstance(RoosterBroadcastIntent roosterIntent) {
        HospitalAvailableBroadcastIntent intent = (HospitalAvailableBroadcastIntent) roosterIntent;
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, intent.getBuildingId());

        HospitalDialogFragment fragment = new HospitalDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);
    }

    @Override
    protected void doInBackground() {
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
    }

    @Override
    protected void onPostExecute() {
        // Populate header.
        String buildingName = StringUtil.capitalize(buildingWithType.getBuildingType().getName());
        buildingNameTextView.setText(buildingName);
        GlideUtil.loadBuildingIcon(buildingIconImageView, buildingWithType);

        populateWoundedSection();
        populateHospitalizedSection();
        populateRecoveredSection();
        populatePeasantSection();
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
            new GridLayoutRowViewHolder(woundedGridLayout, R.layout.list_item_wounded_unit) {

                @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
                @BindView(R.id.health_text_view) TextView healthTextView;
                @BindView(R.id.admit_button) Button admitButton;

                @Override
                protected void loadData() {
                    String healthStr = getString(
                            R.string.wounded_health,
                            unitWithType.getUnit().getHealth(),
                            unitWithType.getType().getHealth());

                    unitTypeNameTextView.setText(unitWithType.getType().getName());
                    healthTextView.setText(healthStr);

                    admitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onAdmitWoundedUnitClicked(unitWithType);
                        }
                    });
                }
            };
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

        final MutableLong healingDurationSum = new MutableLong(0);
        for (final UnitWithType unitWithType : hospitalizedUnitWithTypes) {
            new GridLayoutRowViewHolder(
                    hospitalizedGridLayout,
                    R.layout.list_item_hospitalized_unit) {

                @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
                @BindView(R.id.health_text_view) TextView healthTextView;
                @BindView(R.id.estimated_time_text_view) TextView estimatedTimeTextView;

                @Override
                protected void loadData() {
                    String healthStr = getString(
                            R.string.wounded_health,
                            unitWithType.getUnit().getHealth(),
                            unitWithType.getType().getHealth());

                    long healingDuration = HospitalService.computeHealingDuration(
                            unitWithType,
                            peasantCountInBuilding);
                    healingDurationSum.add(healingDuration);
                    healingDurationSum.divide(60 * 1_000);
                    String estimateStr =
                            getString(R.string.healing_estimate, healingDurationSum.getValue());

                    unitTypeNameTextView.setText(unitWithType.getType().getName());
                    healthTextView.setText(healthStr);
                    estimatedTimeTextView.setText(estimateStr);
                }
            };
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
        recoveredHeadingTextView.setVisibility(View.VISIBLE);
        recoveredGridLayout.setVisibility(View.VISIBLE);
        recoveredGridLayout.removeAllViews();

        for (final UnitWithType unitWithType : recoveredUnitWithTypes) {
            new GridLayoutRowViewHolder(recoveredGridLayout, R.layout.list_item_recovered_unit) {

                @BindView(R.id.unit_type_name_text_view) TextView unitTypeNameTextView;
                @BindView(R.id.pick_up_button) Button pickUpButton;

                @Override
                protected void loadData() {
                    unitTypeNameTextView.setText(unitWithType.getType().getName());

                    pickUpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onPickUpRecoveredUnitClicked(unitWithType);
                        }
                    });
                }
            };
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
            assignPeasantButton.setEnabled(true);
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
                RoosterDaoUtil.transferUnitToBuilding(
                        getActivity(),
                        GameConfig.PEASANT_ID,
                        buildingId);
            }
        });
    }

    private void onAdmitWoundedUnitClicked(final UnitWithType unitWithType) {
        reload(new Runnable() {
            @Override
            public void run() {
                RoosterDaoUtil.transferUnitToBuilding(
                        getActivity(),
                        unitWithType.getUnit(),
                        buildingId);
            }
        });
    }

    private void onPickUpRecoveredUnitClicked(final UnitWithType unitWithType) {
        reload(new Runnable() {
            @Override
            public void run() {
                RoosterDaoUtil.transferUnitFromBuilding(
                        getActivity(),
                        unitWithType.getUnit(),
                        buildingId);
            }
        });
    }
}
