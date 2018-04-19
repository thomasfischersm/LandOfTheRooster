package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.glide.GlideApp;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.playposse.landoftherooster.contentprovider.room.RoosterDaoUtil.PRODUCTION_CYCLE_MS;
import static com.playposse.landoftherooster.dialog.PeasantActionData.MAX_PEASANT_BUILDING_CAPACITY;

/**
 * A dialog that lets the user drop off resources, pickup resources, and assign peasants to a
 * building.
 */
public final class BuildingInteractionDialog {

    /**
     * Base amount of peasants in each building. A building is implied to have at least one peasant,
     * who cannot leave. To save database space, this peasant doesn't have a unit instance.
     */
    public static final int IMPLIED_PEASANT_COUNT = 1;

    private static final int PEASANT_ID = 1;
    private static final String NEW_LINE_CHARACTER = "\n";
    private static final String LIST_SEPARATOR = ", ";

    private BuildingInteractionDialog() {
    }

    public static void show(Context context, long buildingId) {
        new LoadingAsyncTask(context, buildingId).execute();
    }

    /**
     * An {@link AsyncTask} that loads the data and then shows the dialog.
     */
    static class LoadingAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final long buildingId;

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

        private RoosterDao dao;
        private BuildingWithType buildingWithType;
        private BuildingType buildingType;
        private List<ProductionRule> productionRules;
        private String productionRulesStr;
        private List<ActionData> actions = new ArrayList<>();
        private int peasantCount;
        private View rootView;
        private ScheduledExecutorService scheduledExecutorService;
        private BuildingProximityDialogReceiver proximityReceiver;

        private LoadingAsyncTask(Context context, long buildingId) {
            this.context = context;
            this.buildingId = buildingId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao = RoosterDatabase.getInstance(context).getDao();
            buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);

            buildingType = buildingWithType.getBuildingType();
            int buildingTypeId = buildingType.getId();
            productionRules = dao.getProductionRulesByBuildingTypeId(buildingTypeId);
            peasantCount = dao.getUnitCount(PEASANT_ID, buildingId) + IMPLIED_PEASANT_COUNT;

            productionRulesStr = generateProductionRulesStr();

            generateActions();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Detect if the user walked away from the building.
            proximityReceiver = new BuildingProximityDialogReceiver(context);

            // Create custom layout.
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            rootView = layoutInflater.inflate(R.layout.dialog_building_interaction, null);
            populateView();


            // Create dialog
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(rootView)
                    .setPositiveButton(
                            R.string.return_to_map_button_label,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (scheduledExecutorService != null) {
                                        scheduledExecutorService.shutdownNow();
                                    }

                                    dialog.dismiss();
                                }
                            })
                    .show();

            proximityReceiver.setDialog(dialog);
        }

        private void populateView() {
            ButterKnife.bind(this, rootView);

            String buildingName = StringUtil.capitalize(buildingType.getName());
            buildingNameTextView.setText(buildingName);
            populateProductionRules();

            populateActionGrid();

            populateSpeed();

            populateCountdownClock();

            int drawableId = context.getResources().getIdentifier(
                    buildingType.getIcon(),
                    "drawable",
                    context.getPackageName());
            GlideApp.with(context)
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
                        String freeTemplate = context.getString(
                                R.string.free_production_rule_template,
                                outputStr);
                        productionRulesBuilder.append(freeTemplate);
                    } else {
                        String template = context.getString(
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
            StringBuilder sb = new StringBuilder();

            List<Integer> resourceTypeIds =
                    StringUtil.splitToIntList(productionRule.getInputResourceTypeIds());
            for (int resourceTypeId : resourceTypeIds) {
                if (sb.length() > 0) {
                    sb.append(LIST_SEPARATOR);
                }

                // TODO: Optimize loading of strings to a single query.
                ResourceType resourceType = dao.getResourceTypeById(resourceTypeId);
                sb.append("1 " + resourceType.getName());
            }

            List<Integer> unitTypeIds =
                    StringUtil.splitToIntList(productionRule.getInputUnitTypeIds());
            for (int unitTypeId : unitTypeIds) {
                if (sb.length() > 0) {
                    sb.append(LIST_SEPARATOR);
                }

                // TODO: Optimize loading of strings to a single query.
                UnitType unitType = dao.getUnitTypeById(unitTypeId);
                sb.append("1 " + unitType.getName());
            }

            return sb.toString();
        }

        private String formatProductionOutput(ProductionRule productionRule) {
            StringBuilder sb = new StringBuilder();

            Integer resourceTypeId = productionRule.getOutputResourceTypeId();
            if (resourceTypeId != null) {
                // TODO: Optimize loading of strings to a single query.
                ResourceType resourceType = dao.getResourceTypeById(resourceTypeId);
                sb.append("1 " + resourceType.getName());
            }

            Integer unitTypeId = productionRule.getOutputUnitTypeId();
            if (unitTypeId != null) {
                // TODO: Optimize loading of strings to a single query.
                UnitType unitType = dao.getUnitTypeById(unitTypeId);
                sb.append("1 " + unitType.getName());
            }

            return sb.toString();
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
                            context,
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
                            context,
                            dao,
                            inputUnitTypeId,
                            buildingId,
                            ActionData.ActionType.DROP_OFF));
                }

                // output resource
                Integer outputResourceTypeId = productionRule.getOutputResourceTypeId();
                if (outputResourceTypeId != null) {
                    actions.add(new ResourceActionData(
                            context,
                            dao,
                            outputResourceTypeId,
                            buildingId,
                            ResourceActionData.ActionType.PICKUP));
                }

                // output unit
                Integer outputUnitTypeId = productionRule.getOutputUnitTypeId();
                if (outputUnitTypeId != null) {
                    actions.add(new UnitActionData(
                            context,
                            dao,
                            outputUnitTypeId,
                            buildingId,
                            ResourceActionData.ActionType.PICKUP));
                }
            }

            // Generate action to add peasant.
            actions.add(new PeasantActionData(context, dao, PEASANT_ID, buildingId));
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
                LayoutInflater layoutInflater = LayoutInflater.from(context);
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
                        new RefreshViewAsyncTask(action, LoadingAsyncTask.this).execute();
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
            sb.append(context.getString(R.string.production_speed, peasantCount, cycleTime));

            if (peasantCount < MAX_PEASANT_BUILDING_CAPACITY) {
                int nextCycleTime =
                        RoosterDaoUtil.getProductionSpeedInMinutes(peasantCount + 1);
                sb.append(context.getString(R.string.production_speed_hint, nextCycleTime));
            }

            productionSpeedTextView.setText(sb.toString());
        }

        private void populateCountdownClock() {
            // Cancel old timer.
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdownNow();
                scheduledExecutorService = null;
            }

            // Skip if the building produces nothing.
            if (productionRules.size() == 0) {
                setCountdownVisibility(GONE);
                return;
            }

            // TODO: Check if the item is already produced.

            setCountdownVisibility(VISIBLE);

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
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            CountdownUpdateRunnable countdownTask =
                    new CountdownUpdateRunnable(countDownTextView, remainingMs);
            scheduledExecutorService.scheduleAtFixedRate(countdownTask, 1, 1, TimeUnit.SECONDS);

            proximityReceiver.setScheduledExecutorService(scheduledExecutorService);
        }

        private void setCountdownVisibility(int visibility) {
            countDownHeadingTextView.setVisibility(visibility);
            countDownTextView.setVisibility(visibility);
        }
    }

    /**
     * An {@link AsyncTask} that loads fresh data and updates the view.
     */
    private static class RefreshViewAsyncTask extends AsyncTask<Void, Void, Void> {

        private final ActionData action;
        private final LoadingAsyncTask loadingAsyncTask;

        private RefreshViewAsyncTask(ActionData action, LoadingAsyncTask loadingAsyncTask) {
            this.action = action;
            this.loadingAsyncTask = loadingAsyncTask;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            action.performAction();

            loadingAsyncTask.doInBackground();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingAsyncTask.populateView();
        }
    }
}
