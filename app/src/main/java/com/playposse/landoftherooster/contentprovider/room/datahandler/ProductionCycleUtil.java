package com.playposse.landoftherooster.contentprovider.room.datahandler;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitCountByType;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.playposse.landoftherooster.GameConfig.IMPLIED_PEASANT_COUNT;
import static com.playposse.landoftherooster.GameConfig.PEASANT_ID;
import static com.playposse.landoftherooster.GameConfig.PRODUCTION_CYCLE_MS;

/**
 * A utility class that helps dealing with the production cycle of buildings.
 */
public final class ProductionCycleUtil {

    private ProductionCycleUtil() {}

    /**
     * Sets the productionStart on the building. Production of non-free resources/units should
     * start when input resources/inputs are dropped off at the building.
     */
    public static void setProductionStartOnDropOff(
            RoosterDao dao,
            BuildingWithType buildingWithType) {

        // Skip if the production cycle has already started.
        Building building = buildingWithType.getBuilding();
        if (building.getProductionStart() != null) {
            return;
        }

        // Check if all required inputs are in the building to start production.
        if (canBuildingStartProducing(dao, building)) {
            setBuildingProductionStart(dao, building);
        }
    }

    public static void setProductionStartOnPickup(
            RoosterDao dao,
            BuildingWithType buildingWithType) {

        // Skip if the production cycle has already started.
        Building building = buildingWithType.getBuilding();
        if (building.getProductionStart() != null) {
            return;
        }

        if (hasUnblockedFreeProductionRule(dao, building)) {
            setBuildingProductionStart(dao, building);
        }
    }

    @Nullable
    public static Long getRemainingProductionTimeMs(
            RoosterDao dao,
            BuildingWithType buildingWithType) {

        Building building = buildingWithType.getBuilding();

        // Check if no production is running.
        if (building.getProductionStart() == null) {
            return null;
        }

        // Get peasants working in building.
        int peasantCount = dao.getUnitCount(PEASANT_ID, building.getId()) + IMPLIED_PEASANT_COUNT;

        long cycleMs = PRODUCTION_CYCLE_MS / peasantCount;
        long startMs = building.getProductionStart().getTime();
        long remainingMs = startMs + cycleMs - System.currentTimeMillis();
        return Math.max(remainingMs, 0);
    }

    private static boolean hasUnblockedFreeProductionRule(RoosterDao dao, Building building) {
        // Load resources and unit counts.
        Map<Long, Integer> resourceMap = getResourcesInBuilding(dao, building);
        Map<Long, Integer> unitMap = getUnitCountsInBuilding(dao, building);

        // Check if there is a free ProductionRule.
        List<ProductionRule> productionRules =
                dao.getProductionRulesByBuildingTypeId(building.getBuildingTypeId());

        for (ProductionRule productionRule : productionRules) {
            // Skip non-free production rules.
            if (!productionRule.isFree()) {
                continue;
            }

            // Skip free production rules that are blocked by output waiting for pickup.
            if (isFreeProductionRuleBlocked(resourceMap, unitMap, productionRule)) {
                continue;
            }

            return true;
        }

        return false;
    }

    public static boolean isFreeProductionRuleBlocked(
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap,
            ProductionRule productionRule) {

        // Check if the output resource is still waiting to be picked up.
        Long resourceTypeId = productionRule.getOutputResourceTypeId();
        if ((resourceTypeId != null)
                && resourceMap.containsKey(resourceTypeId)
                && (resourceMap.get(resourceTypeId) > 0)) {
            return true;
        }

        // Check if the output unit type is still waiting to be picked up.
        Long unitTypeId = productionRule.getOutputUnitTypeId();
        if ((unitTypeId != null)
                && unitMap.containsKey(unitTypeId)
                && (unitMap.get(unitTypeId) > 0)) {
            return true;
        }
        return false;
    }

    private static boolean canBuildingStartProducing(RoosterDao dao, Building building) {
        // Load resources and unit counts.
        Map<Long, Integer> resourceMap = getResourcesInBuilding(dao, building);
        Map<Long, Integer> unitMap = getUnitCountsInBuilding(dao, building);

        List<ProductionRule> productionRules =
                dao.getProductionRulesByBuildingTypeId(building.getBuildingTypeId());

        for (ProductionRule productionRule : productionRules) {
            if (hasProductionInputs(resourceMap, unitMap, productionRule)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasProductionInputs(
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap,
            ProductionRule productionRule) {

        // Skip free production rules.
        if (productionRule.isFree()) {
            return true;
        }

        // Check resource inputs.
        List<Long> inputResourceTypeIds =
                StringUtil.splitToLongList(productionRule.getInputResourceTypeIds());
        for (long resourceTypeId : inputResourceTypeIds) {
            // Skip if input resource is missing.
            if (!resourceMap.containsKey(resourceTypeId)
                    || (resourceMap.get(resourceTypeId) < 1)) {
                return false;
            }
        }

        // Check unit inputs.
        List<Long> inputUnitTypeIds =
                StringUtil.splitToLongList(productionRule.getInputUnitTypeIds());
        for (long unitTypeId : inputUnitTypeIds) {
            if (!unitMap.containsKey(unitTypeId) || (unitMap.get(unitTypeId) < 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Builds a map (resourceTypeId -> amount) for how many resources a building has stored.
     */
    public static Map<Long, Integer> getResourcesInBuilding(RoosterDao dao, Building building) {
        Map<Long, Integer> resourceMap = new HashMap<>();
        List<Resource> resources = dao.getResourcesByBuildingId(building.getId());
        for (Resource resource : resources) {
            resourceMap.put(resource.getResourceTypeId(), resource.getAmount());
        }
        return resourceMap;
    }

    /**
     * Builds a map (unitTypeId -> unitCount) for how many units of each type are inside of a
     * building.
     */
    public static Map<Long, Integer> getUnitCountsInBuilding(RoosterDao dao, Building building) {
        Map<Long, Integer> unitMap = new HashMap<>();
        List<UnitCountByType> unitCounts = dao.getUnitCountByBuilding(building.getId());
        for (UnitCountByType unitCount : unitCounts) {
            unitMap.put(unitCount.getUnitTypeId(), unitCount.getCount());
        }
        return unitMap;
    }

    private static void setBuildingProductionStart(RoosterDao dao, Building building) {
        building.setProductionStart(new Date());
        dao.update(building);
    }
}
