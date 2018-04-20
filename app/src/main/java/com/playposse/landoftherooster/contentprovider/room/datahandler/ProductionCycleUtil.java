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

            // Check if the output resource is still waiting to be picked up.
            Long resourceTypeId = productionRule.getOutputResourceTypeId();
            if ((resourceTypeId != null)
                    && resourceMap.containsKey(resourceTypeId)
                    && (resourceMap.get(resourceTypeId) > 0)) {
                continue;
            }

            // Check if the output unit type is still waiting to be picked up.
            Long unitTypeId = productionRule.getOutputUnitTypeId();
            if ((unitTypeId != null)
                    && unitMap.containsKey(unitTypeId)
                    && (unitMap.get(unitTypeId) > 0)) {
                continue;
            }

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

        nextProductionRule: for (ProductionRule productionRule : productionRules) {
            // Skip free production rules.
            if (productionRule.isFree()) {
                continue;
            }

            // Check resource inputs.
            List<Long> inputResourceTypeIds =
                    StringUtil.splitToLongList(productionRule.getInputResourceTypeIds());
            for (long resourceTypeId : inputResourceTypeIds) {
                // Skip if input resource is missing.
                if (!resourceMap.containsKey(resourceTypeId)
                        || (resourceMap.get(resourceTypeId) < 1)) {
                    continue nextProductionRule;
                }
            }

            // Check unit inputs.
            List<Long> inputUnitTypeIds =
                    StringUtil.splitToLongList(productionRule.getInputUnitTypeIds());
            for (long unitTypeId : inputUnitTypeIds) {
                if (!unitMap.containsKey(unitTypeId) || (unitMap.get(unitTypeId) < 1)) {
                    continue nextProductionRule;
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Builds a map (resourceTypeId -> amount) for how many resources a building has stored.
     */
    private static Map<Long, Integer> getResourcesInBuilding(RoosterDao dao, Building building) {
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
    private static Map<Long, Integer> getUnitCountsInBuilding(RoosterDao dao, Building building) {
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