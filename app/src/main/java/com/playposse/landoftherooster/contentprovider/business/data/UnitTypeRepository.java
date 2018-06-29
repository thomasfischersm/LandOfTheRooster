package com.playposse.landoftherooster.contentprovider.business.data;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to cache all the {@link UnitType}s to avoid having to access the database.
 */
public final class UnitTypeRepository {

    private static UnitTypeRepository instance;

    private final RoosterDao dao;
    private final Map<Long, UnitType> idToUnitTypeMap = new HashMap<>();

    private UnitTypeRepository(RoosterDao dao) {
        this.dao = dao;

        init();
    }

    public static UnitTypeRepository get(RoosterDao dao) {
        if (instance == null) {
            instance = new UnitTypeRepository(dao);
        }
        return instance;
    }

    private void init() {
        List<UnitType> unitTypes = dao.getAllUnitTypes();
        for (UnitType unitType : unitTypes) {
            idToUnitTypeMap.put(unitType.getId(), unitType);
        }
    }

    public UnitType getUnitType(long unitTypeId) {
        return idToUnitTypeMap.get(unitTypeId);
    }

    public UnitWithType getUnitWithType(Unit unit) {
        if (unit == null) {
            return null;
        }

        UnitType unitType = idToUnitTypeMap.get(unit.getUnitTypeId());
        return new UnitWithType(unit, unitType);
    }

    public UnitWithType queryUnitWithTypeById(long unitId) {
        Unit unit = dao.getUnitById(unitId);
        UnitType unitType = idToUnitTypeMap.get(unit.getUnitTypeId());
        return new UnitWithType(unit, unitType);
    }

    public List<UnitType> getFriendlyUnitTypes() {
        BuildingTypeRepository buildingTypeRepository = BuildingTypeRepository.get(dao);
        List<BuildingType> buildingTypes = buildingTypeRepository.getAllBuildingTypes();

        List<UnitType> friendlyUnitTypes = new ArrayList<>();
        nextUnitType: for (UnitType unitType : idToUnitTypeMap.values()) {
            for (BuildingType buildingType : buildingTypes) {
                if ((buildingType.getEnemyUnitTypeId() != null)
                        && (buildingType.getEnemyUnitTypeId() == unitType.getId())) {
                    continue nextUnitType;
                }
            }

            friendlyUnitTypes.add(unitType);
        }

        return friendlyUnitTypes;
    }

    public List<UnitType> getEnemyUnitTypes() {
        BuildingTypeRepository buildingTypeRepository = BuildingTypeRepository.get(dao);
        List<BuildingType> buildingTypes = buildingTypeRepository.getAllBuildingTypes();

        List<UnitType> enemyUnitTypes = new ArrayList<>();
        nextUnitType: for (UnitType unitType : idToUnitTypeMap.values()) {
            for (BuildingType buildingType : buildingTypes) {
                if ((buildingType.getEnemyUnitTypeId() != null)
                        && (buildingType.getEnemyUnitTypeId() == unitType.getId())) {
                    enemyUnitTypes.add(unitType);
                    continue nextUnitType;
                }
            }
        }

        return enemyUnitTypes;
    }
}