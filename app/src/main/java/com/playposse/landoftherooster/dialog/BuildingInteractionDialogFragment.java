package com.playposse.landoftherooster.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.datahandler.InputResourceTypeIterator;
import com.playposse.landoftherooster.contentprovider.room.datahandler.InputUnitTypeIterator;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.glide.GlideApp;
import com.playposse.landoftherooster.services.broadcastintent.BuildingAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;
import com.playposse.landoftherooster.util.SimpleStringJoiner;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

import static android.view.View.GONE;
import static com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil.PRODUCTION_CYCLE_MS;
import static com.playposse.landoftherooster.dialog.PeasantActionData.MAX_PEASANT_BUILDING_CAPACITY;

/**
 * A dialog that lets the user drop off resources, pickup resources, and assign peasants to a
 * building.
 */
public class BuildingInteractionDialogFragment extends BaseDialogFragment {

    /**
     * Base amount of peasants in each building. A building is implied to have at least one peasant,
     * who cannot leave. To save database space, this peasant doesn't have a unit instance.
     */
    public static final int IMPLIED_PEASANT_COUNT = 1;

    private static final int PEASANT_ID = 1;
    private static final String NEW_LINE_CHARACTER = "\n";
    private static final String LIST_SEPARATOR = ", ";
    private static final String BUILDING_ID_ARG = "buildingId";

    private long buildingId;
    private RoosterDao dao;
    private BuildingWithType buildingWithType;
    private BuildingType buildingType;
    private List<ProductionRule> productionRules;
    private String productionRulesStr;
    private List<ActionData> actions = new ArrayList<>();
    private int peasantCount;

    @BindView(R.id.building_name_text_view) TextView buildingNameTextView;
    @BindView(R.id.building_icon_image_view) ImageView buildingIconImageView;
    @BindView(R.id.action_heading_text_view) TextView actionHeadingTextView;
    @BindView(R.id.action_grid_layout) GridLayout actionGridLayout;
    @BindView(R.id.production_rules_heading_text_view) TextView productionRulesHeadingTextView;
    @BindView(R.id.production_rules_text_view) TextView productionRulesTextView;
    @BindView(R.id.production_speed_heading_text_view) TextView productionSpeedHeadingTextView;
    @BindView(R.id.production_speed_text_view) TextView productionSpeedTextView;
    @BindView(R.id.countdown_heading_text_view) TextView countDownHeadingTextView;
    @BindView(R.id.countdown_text_view) TextView countDownTextView;

    public BuildingInteractionDialogFragment() {
        super(R.layout.dialog_building_interaction);

        setShowReturnToMapButton(true);
        setDisappearOnDistance(true);
    }

    public static BuildingInteractionDialogFragment newInstance(
            RoosterBroadcastIntent roosterIntent) {

        BuildingAvailableBroadcastIntent intent = (BuildingAvailableBroadcastIntent) roosterIntent;
        long buildingId = intent.getBuildingId();

        BuildingInteractionDialogFragment fragment = new BuildingInteractionDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected void doInBackground() {
        dao = RoosterDatabase.getInstance(getActivity()).getDao();
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);

        buildingType = buildingWithType.getBuildingType();
        int buildingTypeId = buildingType.getId();
        productionRules = dao.getProductionRulesByBuildingTypeId(buildingTypeId);
        peasantCount = dao.getUnitCount(PEASANT_ID, buildingId) + IMPLIED_PEASANT_COUNT;

        productionRulesStr = generateProductionRulesStr();

        generateActions();
    }

    @Override
    protected void onPostExecute() {
        String buildingName = StringUtil.capitalize(buildingType.getName());
        buildingNameTextView.setText(buildingName);
        populateProductionRules();

        populateActionGrid();

        populateSpeed();

        populateCountdownClock();

        int drawableId = getActivity().getResources().getIdentifier(
                buildingType.getIcon(),
                "drawable",
                getActivity().getPackageName());
        GlideApp.with(getActivity())
                .load(drawableId)
                .into(buildingIconImageView);

    }

    private void populateProductionRules() {
        if (productionRules.size() == 0) {
            productionRulesHeadingTextView.setVisibility(GONE);
            productionRulesTextView.setVisibility(GONE);
            return;
        }

        productionRulesHeadingTextView.setVisibility(View.VISIBLE);
        productionRulesTextView.setVisibility(View.VISIBLE);

        productionRulesTextView.setText(productionRulesStr);
    }

    private String generateProductionRulesStr() {
        StringBuilder productionRulesBuilder = new StringBuilder();
        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                String inputStr = formatProductionInput(productionRule);
                String outputStr = formatProductionOutput(productionRule);

                if (productionRulesBuilder.length() > 0) {
                    productionRulesBuilder.append(NEW_LINE_CHARACTER);
                }

                if (productionRule.isFree()) {
                    String freeTemplate = getActivity().getString(
                            R.string.free_production_rule_template,
                            outputStr);
                    productionRulesBuilder.append(freeTemplate);
                } else {
                    String template = getActivity().getString(
                            R.string.production_rule_template,
                            inputStr,
                            outputStr);
                    productionRulesBuilder.append(template);
                }
            }
        }

        return productionRulesBuilder.toString();
    }

    private String formatProductionInput(ProductionRule productionRule) {
        SimpleStringJoiner joiner =
                new SimpleStringJoiner(LIST_SEPARATOR, "1 ", null);

        for (ResourceType resourceType : new InputResourceTypeIterator(dao, productionRule)) {
            joiner.add(resourceType.getName());
        }

        for (UnitType unitType : new InputUnitTypeIterator(dao, productionRule)) {
            joiner.add(unitType.getName());
        }

        return joiner.toString();
    }

    private String formatProductionOutput(ProductionRule productionRule) {
        SimpleStringJoiner joiner =
                new SimpleStringJoiner(LIST_SEPARATOR, "1 ", null);

        Integer resourceTypeId = productionRule.getOutputResourceTypeId();
        if (resourceTypeId != null) {
            // TODO: Optimize loading of strings to a single query.
            ResourceType resourceType = dao.getResourceTypeById(resourceTypeId);
            joiner.add(resourceType.getName());
        }

        Integer unitTypeId = productionRule.getOutputUnitTypeId();
        if (unitTypeId != null) {
            // TODO: Optimize loading of strings to a single query.
            UnitType unitType = dao.getUnitTypeById(unitTypeId);
            joiner.add(unitType.getName());
        }

        return joiner.toString();
    }

    private void generateActions() {
        actions.clear();

        // Generate actions based on production rules.
        for (ProductionRule productionRule : productionRules) {
            // input resources
            List<Integer> inputResourceTypeIds =
                    StringUtil.splitToIntList(productionRule.getInputResourceTypeIds());
            for (int inputResourceTypeId : inputResourceTypeIds) {
                actions.add(new ResourceActionData(
                        getActivity(),
                        dao,
                        inputResourceTypeId,
                        buildingId,
                        ResourceActionData.ActionType.DROP_OFF));
            }

            // input units
            List<Integer> inputUnitTypeIds =
                    StringUtil.splitToIntList(productionRule.getInputUnitTypeIds());
            for (int inputUnitTypeId : inputUnitTypeIds) {
                actions.add(new UnitActionData(
                        getActivity(),
                        dao,
                        inputUnitTypeId,
                        buildingId,
                        ActionData.ActionType.DROP_OFF));
            }

            // output resource
            Integer outputResourceTypeId = productionRule.getOutputResourceTypeId();
            if (outputResourceTypeId != null) {
                actions.add(new ResourceActionData(
                        getActivity(),
                        dao,
                        outputResourceTypeId,
                        buildingId,
                        ResourceActionData.ActionType.PICKUP));
            }

            // output unit
            Integer outputUnitTypeId = productionRule.getOutputUnitTypeId();
            if (outputUnitTypeId != null) {
                actions.add(new UnitActionData(
                        getActivity(),
                        dao,
                        outputUnitTypeId,
                        buildingId,
                        ResourceActionData.ActionType.PICKUP));
            }
        }

        // Generate action to add peasant.
        actions.add(new PeasantActionData(getActivity(), dao, PEASANT_ID, buildingId));
    }

    private void populateActionGrid() {
        actionGridLayout.removeAllViews();

        if (actions.size() == 0) {
            // Hide actions.
            actionGridLayout.setVisibility(GONE);
            actionHeadingTextView.setVisibility(GONE);
            return;
        }

        actionGridLayout.setVisibility(View.VISIBLE);
        actionHeadingTextView.setVisibility(View.VISIBLE);

        for (final ActionData action : actions) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            ViewGroup rootView = (ViewGroup) layoutInflater.inflate(
                    R.layout.list_item_building_action,
                    null);

            TextView userSideTextView = rootView.findViewById(R.id.user_side_text_view);
            rootView.removeView(userSideTextView);
            userSideTextView.setText(action.getUserString());
            actionGridLayout.addView(userSideTextView);

            Button button = rootView.findViewById(R.id.action_button);
            rootView.removeView(button);
            button.setText(action.getActionString());
            button.setEnabled(action.isAvailable());
            actionGridLayout.addView(button);

            TextView buildingSideTextView = rootView.findViewById(R.id.building_side_text_view);
            rootView.removeView(buildingSideTextView);
            buildingSideTextView.setText(action.getBuildingString());
            actionGridLayout.addView(buildingSideTextView);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reload(action);
                }
            });
        }
    }

    private void populateSpeed() {
        if (productionRules.size() == 0) {
            // Hide production speed if the building produces nothing.
            productionSpeedHeadingTextView.setVisibility(GONE);
            productionSpeedTextView.setVisibility(GONE);
            return;
        }

        productionSpeedHeadingTextView.setVisibility(View.VISIBLE);
        productionSpeedTextView.setVisibility(View.VISIBLE);

        StringBuilder sb = new StringBuilder();
        int cycleTime = RoosterDaoUtil.getProductionSpeedInMinutes(peasantCount);
        sb.append(getActivity().getString(R.string.production_speed, peasantCount, cycleTime));

        if (peasantCount < MAX_PEASANT_BUILDING_CAPACITY) {
            int nextCycleTime =
                    RoosterDaoUtil.getProductionSpeedInMinutes(peasantCount + 1);
            sb.append(getActivity().getString(R.string.production_speed_hint, nextCycleTime));
        }

        productionSpeedTextView.setText(sb.toString());
    }

    private void populateCountdownClock() {
        // Skip if the building produces nothing.
        if (productionRules.size() == 0) {
            clearCountdown(countDownHeadingTextView, countDownTextView);
            return;
        }

        // TODO: Check if the item is already produced.

        // Calculate remaining time.
        Date lastProduction = buildingWithType.getBuilding().getLastProduction();
        if (lastProduction == null) {
            return;
        }
        long lastProductionMs = lastProduction.getTime();
        long remainingMs = lastProductionMs + PRODUCTION_CYCLE_MS - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return;
        }

        // Start countdown timer.
        startCountdown(countDownHeadingTextView, countDownTextView, remainingMs);
    }
}
