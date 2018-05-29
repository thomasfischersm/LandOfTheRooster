package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepository;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepository;
import com.playposse.landoftherooster.contentprovider.business.precondition.UpdateBuildingMarkerPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A {@link BusinessAction} that updates {@link MapMarker}s.
 */
public class UpdateProductionBuildingMarkerAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();
        ProductionRuleRepository productionRuleRepository =
                ProductionRuleRepository.get(dao);
        UpdateBuildingMarkerPreconditionOutcome outcome =
                (UpdateBuildingMarkerPreconditionOutcome) preconditionOutcome;
        List<BuildingWithType> affectedBuildingWithTypes = outcome.getAffectedBuildingWithTypes();
        List<MapMarker> mapMarkers = queryMapMarkers(affectedBuildingWithTypes, dao);
        Map<Long, Integer> resourceMapJoiningUser = dataCache.getResourceMapJoiningUser();
        Map<Long, Integer> unitMapJoiningUser = dataCache.getUnitMapJoiningUser();

        for (int i = 0; i < affectedBuildingWithTypes.size(); i++) {
            updateBuildingMarker( // updateProductionBuildingMarker & updateHospitalBuildingMarker
                    mapMarkers.get(i),
                    affectedBuildingWithTypes.get(i),
                    productionRuleRepository,
                    resourceMapJoiningUser,
                    unitMapJoiningUser,
                    dao);
        }
    }

    private List<MapMarker> queryMapMarkers(
            List<BuildingWithType> buildingWithTypes,
            RoosterDao dao) {

        // Create list of building ids.
        List<Long> buildingIds = new ArrayList<>();
        for (BuildingWithType buildingWithType : buildingWithTypes) {
            buildingIds.add(buildingWithType.getBuilding().getId());
        }

        // Query db.
        List<MapMarker> mapMarkers = dao.getMapMarkerByBuildingIds(buildingIds);

        // Ensure that both lists point to the same item by sorting by building id.
        Collections.sort(
                mapMarkers,
                new Comparator<MapMarker>() {
                    @Override
                    public int compare(MapMarker m1, MapMarker m2) {
                        return Long.compare(m1.getBuildingId(), m2.getBuildingId());
                    }
                });

        Collections.sort(
                buildingWithTypes,
                new Comparator<BuildingWithType>() {
                    @Override
                    public int compare(BuildingWithType b1, BuildingWithType b2) {
                        return Long.compare(b1.getBuilding().getId(), b2.getBuilding().getId());
                    }
                }
        );

        return mapMarkers;
    }

    private void updateBuildingMarker(
            MapMarker mapMarker,
            BuildingWithType buildingWithType,
            ProductionRuleRepository productionRuleRepository,
            Map<Long, Integer> resourceMapJoiningUser,
            Map<Long, Integer> unitMapJoiningUser,
            RoosterDao dao) {

        // Assemble information.
        Building building = buildingWithType.getBuilding();
        List<ProductionRule> productionRules =
                productionRuleRepository.getProductionRulesByBuildingTypeId(
                        building.getBuildingTypeId());
        Map<Long, Integer> resourceMap = ProductionCycleUtil.getResourcesInBuilding(dao, building);
        Map<Long, Integer> unitMap = ProductionCycleUtil.getUnitCountsInBuilding(dao, building);
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);

        // Compute building state.
        int newPendingCount = computePendingProductionCount(productionRules, resourceMap, unitMap);
        int newCompletedCount =
                computeCompletedProductionCount(productionRules, resourceMap, unitMap);
        boolean newCanUserDropItem = computeCanUserDropItem(
                productionRules,
                resourceMapJoiningUser,
                unitMapJoiningUser,
                unitTypeRepository);
        boolean newIsReady = (newCompletedCount > 0) || newCanUserDropItem; // TODO: Consider carrying capacity. If there is no carrying capacity, the user cannot pick up items.

        // Determine if the marker has changed.
        if ((mapMarker.getPendingProductionCount() != null)
                && (mapMarker.getPendingProductionCount() == newPendingCount)
                && (mapMarker.getCompletedProductionCount() != null)
                && (mapMarker.getCompletedProductionCount() == newCompletedCount)
                && (mapMarker.isReady() == newIsReady)) {
            // Nothing to do. There is no change.
            return;
        }

        // Update map marker
        mapMarker.setPendingProductionCount(newPendingCount);
        mapMarker.setCompletedProductionCount(newCompletedCount);
        mapMarker.setReady(newIsReady);
        DaoEventRegistry.get(dao)
                .update(mapMarker);
    }

    private int computePendingProductionCount(
            List<ProductionRule> productionRules,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        int totalCount = 0;

        for (ProductionRule productionRule : productionRules) {
            Integer count = null;

            // Get max possible production count with available resources.
            for (long resourceTypeId : productionRule.getSplitInputResourceTypeIds()) {
                if (resourceMap.containsKey(resourceTypeId)) {
                    if (count == null) {
                        count = resourceMap.get(resourceTypeId);
                    } else {
                        count = Math.max(count, resourceMap.get(resourceTypeId));
                    }
                } else {
                    count = 0;
                }
            }

            // Get max possible production count with available units.
            for (long unitTypeId : productionRule.getSplitInputUnitTypeIds()) {
                if (unitMap.containsKey(unitTypeId)) {
                    if (count == null) {
                        count = unitMap.get(unitTypeId);
                    } else {
                        count = Math.max(count, unitMap.get(unitTypeId));
                    }
                } else {
                    count = 0;
                }
            }

            if (count != null) {
                totalCount += count;
            } else {
                // This is a free production rule.
                boolean freeProductionRuleBlocked =
                        ProductionCycleUtil.isFreeProductionRuleBlocked(
                                resourceMap,
                                unitMap,
                                productionRule);
                if (!freeProductionRuleBlocked) {
                    totalCount++;
                }
            }
        }

        return totalCount;
    }

    private int computeCompletedProductionCount(
            List<ProductionRule> productionRules,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        int totalCount = 0;

        for (ProductionRule productionRule : productionRules) {
            Long resourceTypeId = productionRule.getOutputResourceTypeId();
            if (resourceTypeId != null) {
                if (resourceMap.containsKey(resourceTypeId)) {
                    totalCount += resourceMap.get(resourceTypeId);
                }
            }

            Long unitTypeId = productionRule.getOutputUnitTypeId();
            if (unitTypeId != null) {
                if (unitMap.containsKey(unitTypeId)) {
                    totalCount += unitMap.get(unitTypeId);
                }
            }
        }

        return totalCount;
    }

    /**
     * Checks if the user carries a resource or unit that could be dropped off at the building.
     * Units with carrying capacity are ignored because the user may want to keep those in the
     * caravan.
     */
    private boolean computeCanUserDropItem(
            List<ProductionRule> productionRules,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap,
            UnitTypeRepository unitTypeRepository) {

        for (ProductionRule productionRule : productionRules) {
            for (long resourceTypeId : productionRule.getSplitInputResourceTypeIds()) {
                if (resourceMap.containsKey(resourceTypeId)
                        && (resourceMap.get(resourceTypeId) > 0)) {
                    return true;
                }
            }

            for (long unitTypeId : productionRule.getSplitInputUnitTypeIds()) {
                if ((unitTypeId != GameConfig.PEASANT_ID)
                        && unitMap.containsKey(unitTypeId)
                        && (unitMap.get(unitTypeId) > 0)) {

                    UnitType unitType = unitTypeRepository.getUnitType(unitTypeId);
                    if (unitType.getCarryingCapacity() == 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
