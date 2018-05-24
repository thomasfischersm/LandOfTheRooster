package com.playposse.landoftherooster.contentprovider.business;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

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

    public BusinessDataCache(RoosterDao dao, @Nullable Long buildingId) {
        this.dao = dao;
        this.buildingId = buildingId;
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
            buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
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
                productionRules = dao.getProductionRulesByBuildingTypeId(buildingTypeId);
            }
        }

        return productionRules;
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
    public void resetResourceMap() {
        resourceMap = null;
    }

    public void resetUnitMap() {
        unitMap = null;
    }
}
