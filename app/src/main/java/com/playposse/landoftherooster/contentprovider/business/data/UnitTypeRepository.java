package com.playposse.landoftherooster.contentprovider.business.data;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

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
}