package com.playposse.landoftherooster.services.time;

import android.content.Context;
import android.util.Log;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A service that checks all the buildings periodically to see if something is ready to be produced.
 *
 * TODO: Be smart about only running this when a player drops off or picks up a resource as well as
 * when the next productionStart is due. This should limit the required processing power.
 */
public class BuildingProductionService extends PeriodicService {

    private static final String LOG_TAG = BuildingProductionService.class.getSimpleName();

    private static final int CYCLE_MS = 60 * 1_000;

    private final RoosterDao dao;

    public BuildingProductionService(Context context) {
        super(context);

        dao = RoosterDatabase.getInstance(context).getDao();
    }

    @Override
    protected long getPeriodMs() {
        return CYCLE_MS;
    }

    @Override
    protected void run() {
        long start = System.currentTimeMillis();
        List<BuildingWithType> buildingWithTypes = getDao().getAllBuildingsWithType();

        for (BuildingWithType buildingWithType : buildingWithTypes) {
            Building building = buildingWithType.getBuilding();
            long buildingTypeId = building.getBuildingTypeId();
            List<ProductionRule> productionRules =
                    getDao().getProductionRulesByBuildingTypeId(buildingTypeId);

            if (productionRules.size() == 0) {
                continue;
            }

            Map<Long, Integer> resourceMap = ProductionCycleUtil.getResourcesInBuilding(dao, building);
            Map<Long, Integer> unitMap = ProductionCycleUtil.getUnitCountsInBuilding(dao, building);

            for (ProductionRule productionRule : productionRules) {
                evaluate(buildingWithType, productionRule, resourceMap, unitMap);
            }
        }

        long end = System.currentTimeMillis();
        Log.i(LOG_TAG, "run: Finished running BuildingProductionService: " + (end - start));
    }

    private void evaluate(
            BuildingWithType buildingWithType,
            ProductionRule productionRule,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        if (productionRule.isFree()) {
            evaluateFree(buildingWithType, productionRule, resourceMap, unitMap);
            return;
        }

        Building building = buildingWithType.getBuilding();
        if (building.getProductionStart() == null) {
            if (ProductionCycleUtil.hasProductionInputs(resourceMap, unitMap, productionRule)) {
                // Set production start.
                setProductionDate(building);
                return;
            } else {
                // Do nothing. The inputs are missing.
                return;
            }
        }

        long productionEnd =
                building.getProductionStart().getTime() + GameConfig.PRODUCTION_CYCLE_MS;
        if (!isProductionTimeFinished(buildingWithType, unitMap)) {
            // Do nothing. Production hasn't completed yet.
            return;
        }

        if (!ProductionCycleUtil.hasProductionInputs(resourceMap, unitMap, productionRule)) {
            // Do nothing. The inputs are missing.
            return;
        }

        produce(buildingWithType, productionRule, resourceMap, unitMap);
    }

    private void evaluateFree(
            BuildingWithType buildingWithType,
            ProductionRule productionRule,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        Building building = buildingWithType.getBuilding();
        if (building.getProductionStart() == null) {
            boolean isBlocked = ProductionCycleUtil.isFreeProductionRuleBlocked(
                    resourceMap,
                    unitMap,
                    productionRule);
            if (!isBlocked) {
                setProductionDate(building);
                return;
            } else {
                // Do nothing. Previous production output has to be picked up first.
                return;
            }
        }

        long productionEnd =
                building.getProductionStart().getTime() + GameConfig.PRODUCTION_CYCLE_MS;
        if (!isProductionTimeFinished(buildingWithType, unitMap)) {
            // Do nothing. Production hasn't completed yet.
            return;
        }

        produce(buildingWithType, productionRule, resourceMap, unitMap);
    }

    private void produce(
            BuildingWithType buildingWithType,
            ProductionRule productionRule,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        Building building = buildingWithType.getBuilding();
        long buildingId = building.getId();

        // Debit input resources.
        List<Long> InputResourceTypeIds = productionRule.getSplitInputResourceTypeIds();
        for (Long inputResourceTypeId : InputResourceTypeIds) {
            RoosterDaoUtil.creditResource(getDao(), inputResourceTypeId, -1, buildingId);
        }

        // Debit input units.
        List<Long> InputUnitTypeIds = productionRule.getSplitInputUnitTypeIds();
        for (Long inputUnitTypeId : InputUnitTypeIds) {
            RoosterDaoUtil.creditUnit(getDao(), inputUnitTypeId, -1, buildingId);
        }

        // Credit output resource.
        if (productionRule.getOutputResourceTypeId() != null) {
            RoosterDaoUtil.creditResource(
                    getDao(),
                    productionRule.getOutputResourceTypeId(),
                    1,
                    buildingId);
        }

        // Credit output unit.
        if (productionRule.getOutputUnitTypeId() != null) {
            RoosterDaoUtil.creditUnit(
                    getDao(),
                    productionRule.getOutputUnitTypeId(),
                    1,
                    buildingId);
        }

        // Reset maps because resource and unit counts have updated.
        resourceMap.clear();
        resourceMap.putAll(ProductionCycleUtil.getResourcesInBuilding(dao, building));

        unitMap.clear();
        unitMap.putAll(ProductionCycleUtil.getUnitCountsInBuilding(dao, building));

        // Clear or reset production start.
        if (!productionRule.isFree()) {
            if (ProductionCycleUtil.hasProductionInputs(resourceMap, unitMap, productionRule)) {
                setProductionDate(building);
            } else {
                clearProductionStart(building);
            }
        } else {
            clearProductionStart(building);
        }
    }

    private void setProductionDate(Building building) {
        building.setProductionStart(new Date());
        DaoEventRegistry.get(dao).update(building);
    }

    private void clearProductionStart(Building building) {
        building.setProductionStart(null);
        DaoEventRegistry.get(dao).update(building);
    }

    private boolean isProductionTimeFinished(
            BuildingWithType buildingWithType,
            Map<Long, Integer> unitMap) {

        Building building = buildingWithType.getBuilding();

        // Get peasant count.
        final int peasantCount;
        if (unitMap.containsKey(GameConfig.PEASANT_ID)) {
            peasantCount = unitMap.get(GameConfig.PEASANT_ID) + GameConfig.IMPLIED_PEASANT_COUNT;
        } else {
            peasantCount = GameConfig.IMPLIED_PEASANT_COUNT;
        }

        int productionCycleMs = GameConfig.PRODUCTION_CYCLE_MS / peasantCount;
        long productionEnd = building.getProductionStart().getTime() + productionCycleMs;
        return (productionEnd < System.currentTimeMillis());
    }
}