package com.playposse.landoftherooster.contentprovider.business.data;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A class to cache all the {@link BuildingType}s to avoid having to access the database.
 */
public final class BuildingTypeRepository {

    private static final String LOG_TAG = BuildingTypeRepository.class.getSimpleName();

    private static BuildingTypeRepository instance;

    private final RoosterDao dao;
    private final Map<Long, BuildingType> idToBuildingTypeMap = new HashMap<>();

    private BuildingTypeRepository(RoosterDao dao) {
        this.dao = dao;

        init();
    }

    public static BuildingTypeRepository get(RoosterDao dao) {
        if (instance == null) {
            instance = new BuildingTypeRepository(dao);
        }
        return instance;
    }

    private void init() {
        List<BuildingType> buildingTypes = dao.getAllBuildingTypes();
        for (BuildingType buildingType : buildingTypes) {
            idToBuildingTypeMap.put(buildingType.getId(), buildingType);
        }
    }

    public BuildingType getBuildingType(long buildingTypeId) {
        return idToBuildingTypeMap.get(buildingTypeId);
    }

    @Nullable
    public BuildingWithType queryBuildingWithType(long buildingId) {
        Building building = dao.getBuildingById(buildingId);
        if (building != null) {
            BuildingType buildingType = idToBuildingTypeMap.get(building.getBuildingTypeId());
            return new BuildingWithType(building, buildingType);
        } else {
            return null;
        }
    }
}