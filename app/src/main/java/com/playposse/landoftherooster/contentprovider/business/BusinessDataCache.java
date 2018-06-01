package com.playposse.landoftherooster.contentprovider.business;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingTypeRepository;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by thoma on 5/2/2018.
 */
public class BusinessDataCache {

    private final RoosterDao dao;
    @Nullable private final Long buildingId;

    @Nullable private BuildingWithType buildingWithType;
    @Nullable private Building building;
    @Nullable private Long buildingTypeId;
    @Nullable private BuildingType buildingType;
    @Nullable private MapMarker mapMarker;
    @Nullable private List<ProductionRule> productionRules;
    @Nullable private Map<Long, Integer> resourceMap;
    @Nullable private Map<Long, Integer> unitMap;
    @Nullable private Map<Long, Integer> resourceMapJoiningUser;
    @Nullable private Map<Long, Integer> unitMapJoiningUser;
    @Nullable private List<UnitWithType> unitsWithType;
    @Nullable private List<UnitWithType> unitsWithTypeJoiningUser;

    public BusinessDataCache(RoosterDao dao, @Nullable Long buildingId) {
        this.dao = dao;
        this.buildingId = buildingId;
    }

    public BusinessDataCache(RoosterDao dao, @Nullable BuildingWithType buildingWithType) {
        this.dao = dao;
        this.buildingWithType = buildingWithType;

        if ((buildingWithType != null) && (buildingWithType.getBuilding() != null)) {
            buildingId = buildingWithType.getBuilding().getId();
        } else {
            buildingId = null;
        }
    }

    public BusinessDataCache(RoosterDao dao) {
        this.dao = dao;

        buildingId = null;
    }

    public RoosterDao getDao() {
        return dao;
    }

    public long getBuildingId() {
        return buildingId;
    }

    @Nullable
    public BuildingWithType getBuildingWithType() {
        if (buildingWithType == null) {
            buildingWithType = BuildingTypeRepository.get(dao).queryBuildingWithType(buildingId);
        }

        return buildingWithType;
    }

    @Nullable
    public Building getBuilding() {
        if (building == null) {
            BuildingWithType buildingWithType = getBuildingWithType();
            if (buildingWithType != null) {
                building = buildingWithType.getBuilding();
            }
        }

        return building;
    }

    @Nullable
    public BuildingType getBuildingType() {
        if (buildingType == null) {
            BuildingWithType buildingWithType = getBuildingWithType();
            if (buildingWithType != null) {
                buildingType = buildingWithType.getBuildingType();
            }
        }

        return buildingType;
    }

    @Nullable
    public Long getBuildingTypeId() {
        if (buildingTypeId == null) {
            BuildingType buildingType = getBuildingType();
            if (buildingType != null) {
                buildingTypeId = buildingType.getId();
            }
        }
        return buildingTypeId;
    }

    @Nullable
    public MapMarker getMapMarker() {
        if (mapMarker == null) {
            mapMarker = dao.getMapMarkerByBuildingId(getBuildingId());
        }
        return mapMarker;
    }

    @Nullable
    public List<ProductionRule> getProductionRules() {
        if (productionRules == null) {
            Long buildingTypeId = getBuildingTypeId();
            if (buildingTypeId != null) {
                productionRules = ProductionRuleRepository.get(dao)
                        .getProductionRulesByBuildingTypeId(buildingTypeId);
            }
        }

        return productionRules;
    }

    public boolean usesResourceTypeAsInput(long resourceTypeId) {
        List<ProductionRule> productionRules = getProductionRules();
        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                if (productionRule.getSplitInputResourceTypeIds().contains(resourceTypeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean usesUnitTypeAsInput(long unitTypeId) {
        List<ProductionRule> productionRules = getProductionRules();
        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                if (productionRule.getSplitInputUnitTypeIds().contains(unitTypeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public Map<Long, Integer> getResourceMap() {
        if ((resourceMap == null) && (getBuilding() != null)) {
            resourceMap = ProductionCycleUtil.getResourcesInBuilding(dao, getBuilding());
        }
        return resourceMap;
    }

    @Nullable
    public Map<Long, Integer> getUnitMap() {
        if ((unitMap == null) && (getBuilding() != null)) {
            unitMap = ProductionCycleUtil.getUnitCountsInBuilding(dao, getBuilding());
        }
        return unitMap;
    }

    @Nullable
    public Map<Long, Integer> getResourceMapJoiningUser() {
        if (resourceMapJoiningUser == null) {
            resourceMapJoiningUser = ProductionCycleUtil.getResourcesJoiningUser(dao);
        }
        return resourceMapJoiningUser;
    }

    @Nullable
    public Map<Long, Integer> getUnitMapJoiningUser() {
        if (unitMapJoiningUser == null) {
            unitMapJoiningUser = ProductionCycleUtil.getUnitCountsJoiningUser(dao);
        }
        return unitMapJoiningUser;
    }

    public int getPeasantCount() {
        Integer count = getUnitMap().get(GameConfig.PEASANT_ID);
        if (count != null) {
            return count + GameConfig.IMPLIED_PEASANT_COUNT;
        } else {
            return GameConfig.IMPLIED_PEASANT_COUNT;
        }
    }

    public void resetResourceMap() {
        resourceMap = null;
    }

    public void resetUnitMap() {
        unitMap = null;
    }

    @Nullable
    public List<UnitWithType> getUnitsWithTypeJoiningUser() {
        if (unitsWithTypeJoiningUser == null) {
            unitsWithTypeJoiningUser = dao.getUnitsWithTypeJoiningUser();
        }
        return unitsWithTypeJoiningUser;
    }

    @Nullable
    public UnitWithType getUnitWithTypeJoiningUser(long unitId) {
        List<UnitWithType> unitWithTypes = getUnitsWithTypeJoiningUser();
        for (UnitWithType unitWithType : unitWithTypes) {
            if (unitWithType.getUnit().getId() == unitId) {
                return unitWithType;
            }
        }
        return null;
    }

    public boolean hasInjuredUnitJoiningUser() {
        List<UnitWithType> unitsWithType = getUnitsWithTypeJoiningUser();
        if (unitsWithType != null) {
            for (UnitWithType unitWithType : unitsWithType) {
                if (unitWithType.isInjured()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<UnitWithType> getUnitsWithType() {
        if (unitsWithType == null) {
            unitsWithType = dao.getUnitsWithTypeByBuildingId(getBuildingId());
        }
        return unitsWithType;
    }

    public void resetUnitsWithType() {
        unitsWithType = null;
    }

    public List<UnitWithType> getInjuredUnitsWithType() {
        List<UnitWithType> unitsWithType = getUnitsWithType();

        List<UnitWithType> result = new ArrayList<>();
        for (UnitWithType unitWithType : unitsWithType) {
            if (unitWithType.isInjured()) {
                result.add(unitWithType);
            }
        }

        // Sort by least injured unit first.
        Collections.sort(
                result,
                new Comparator<UnitWithType>() {
                    @Override
                    public int compare(UnitWithType u0, UnitWithType u1) {
                        return Integer.compare(u0.getInjury(), u1.getInjury());
                    }
                });
        return result;
    }

    public List<UnitWithType> getInjuredUnitsWithTypeJoiningUser() {
        List<UnitWithType> unitsWithType = getUnitsWithTypeJoiningUser();

        List<UnitWithType> result = new ArrayList<>();
        for (UnitWithType unitWithType : unitsWithType) {
            if (unitWithType.isInjured()) {
                result.add(unitWithType);
            }
        }

        // Sort by least injured unit first.
        Collections.sort(
                result,
                new Comparator<UnitWithType>() {
                    @Override
                    public int compare(UnitWithType u0, UnitWithType u1) {
                        return Integer.compare(u0.getInjury(), u1.getInjury());
                    }
                });
        return result;
    }

    public int getHealingUnitCount() {
        int count = 0;
        for (UnitWithType unitWithType : getUnitsWithType()) {
            if (unitWithType.isInjured()) {
                count++;
            }
        }
        return count;
    }

    public List<UnitWithType> getRecoveredUnitsWithType() {
        List<UnitWithType> unitsWithType = getUnitsWithType();

        List<UnitWithType> result = new ArrayList<>();
        for (UnitWithType unitWithType : unitsWithType) {
            boolean isPeasant = unitWithType.getType().getId() == GameConfig.IMPLIED_PEASANT_COUNT;
            if (!isPeasant && !unitWithType.isInjured()) {
                result.add(unitWithType);
            }
        }
        return result;
    }
    public int getRecoveredUnitCount() {
        int count = 0;
        for (UnitWithType unitWithType : getUnitsWithType()) {
            boolean isPeasant = unitWithType.getType().getId() != GameConfig.PEASANT_ID;
            if (isPeasant && !unitWithType.isInjured()) {
                count++;
            }
        }
        return count;
    }
}
