package com.playposse.landoftherooster;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * A utility class to create test data.
 */
public class TestData {

    public static final long CASTLE_BUILDING_TYPE_ID = 1;
    public static final long WHEAT_FIELD_BUILDING_TYPE_ID = 2; // Mill: wheat (1) -> flour (2)
    public static final long MILL_BUILDING_TYPE_ID = 3; // Mill: wheat (1) -> flour (2)
    public static final long BAKERY_BUILDING_TYPE_ID = 4; // Mill: flour (2) -> bread (3)
    public static final long VILLAGE_BUILDING_TYPE_ID = 5; // Village: bread (3) -> peasant (1)
    public static final long BARRACKS_BUILDING_TYPE_ID = 6; // Barracks: peasant (1) -> soldier (2)
    public static final long GOBLIN_CAVE_BUILDING_TYPE_ID = 7;
    public static final long HOSPITAL_BUILDING_TYPE_ID = 8;

    public static final long WHEAT_RESOURCE_TYPE_ID = 1;
    public static final long FLOUR_RESOURCE_TYPE_ID = 2;

    public static final long SOLDIER_UNIT_TYPE_ID = 2;

    public static final long LATITUDE = 34;
    public static final long LONGITUDE = 118;

    public static final int REMAINING_HEALTH = 1;

    public static long createWheatField(RoosterDao dao) {
        Building building = new Building(WHEAT_FIELD_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    public static long createWheatFieldAndMarker(RoosterDao dao) {
        long wheatFieldId = createWheatField(dao);

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                "",
                "",
                0,
                0,
                false,
                wheatFieldId,
                WHEAT_FIELD_BUILDING_TYPE_ID);
        mapMarker.setLastModified(new Date());
        dao.insert(mapMarker);

        return wheatFieldId;
    }

    public static long createMill(RoosterDao dao) {
        Building building = new Building(MILL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    public static long createMillAndMarker(RoosterDao dao) {
        long millId = createMill(dao);

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                "",
                "",
                0,
                0,
                false,
                millId,
                MILL_BUILDING_TYPE_ID);
        mapMarker.setLastModified(new Date());
        dao.insert(mapMarker);

        return millId;
    }

    public static long createBakery(RoosterDao dao) {
        Building building = new Building(BAKERY_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    public static long createBakeryAndMarker(RoosterDao dao) {
        long bakerId = createBakery(dao);

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                "",
                "",
                0,
                0,
                false,
                bakerId,
                BAKERY_BUILDING_TYPE_ID);
        mapMarker.setLastModified(new Date());
        dao.insert(mapMarker);

        return bakerId;
    }

    public static long createGoblinCave(RoosterDao dao) {
        Building building = new Building(GOBLIN_CAVE_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    public static long createGoblinCaveAndMarker(RoosterDao dao) {
        long goblinCaveId = createGoblinCave(dao);

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                "",
                "",
                0,
                0,
                false,
                goblinCaveId,
                GOBLIN_CAVE_BUILDING_TYPE_ID);
        mapMarker.setLastModified(new Date());
        dao.insert(mapMarker);

        return goblinCaveId;
    }

    public static long createHospital(RoosterDao dao) {
        Building building = new Building(HOSPITAL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    public static long createHospitalAndMarker(RoosterDao dao) {
        long hospitalId = createHospital(dao);

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                "",
                "",
                0,
                0,
                false,
                hospitalId,
                HOSPITAL_BUILDING_TYPE_ID);
        mapMarker.setLastModified(new Date());
        dao.insert(mapMarker);

        return hospitalId;
    }

    public void assertBuildingIds(
            List<BuildingWithType> buildingWithTypes,
            Long... buildingIds) {

        // Make a lookup map.
        Map<Long, BuildingWithType> map = new HashMap<>();
        for (BuildingWithType buildingWithType : buildingWithTypes) {
            long buildingId = buildingWithType.getBuilding().getId();
            map.put(buildingId, buildingWithType);
        }

        // Remove ids.
        for (long buildingId : buildingIds) {
            BuildingWithType buildingWithType = map.remove(buildingId);
            if (buildingWithType == null) {
                fail("The buildingId is not in the list: " + buildingId);
            }
        }

        // Check that all buildings are accounted for.
        if (map.size() > 0) {
            fail("There are buildings with additional ids: " + map.size());
        }
    }

    public void assertBuildingTypeIds(
            List<BuildingWithType> buildingWithTypes,
            Long... buildingTypeIds) {

        // Make a lookup map.
        Map<Long, BuildingWithType> map = new HashMap<>();
        for (BuildingWithType buildingWithType : buildingWithTypes) {
            long buildingTypeId = buildingWithType.getBuilding().getBuildingTypeId();
            map.put(buildingTypeId, buildingWithType);
        }

        // Remove ids.
        for (long buildingTypeId : buildingTypeIds) {
            BuildingWithType buildingWithType = map.remove(buildingTypeId);
            if (buildingWithType == null) {
                fail("The buildingTypeId is not in the list: " + buildingTypeId);
            }
        }

        // Check that all buildings are accounted for.
        if (map.size() > 0) {
            fail("There are buildings with additional ids: " + map.size());
        }
    }

    public Date getLastModifiedForMarker(RoosterDao dao, long buildingId) {
        List<MapMarker> marker = dao.getMapMarkerByBuildingIds(Arrays.asList(buildingId));
        return marker.get(0).getLastModified();
    }

    public void createUnitsJoiningUser(RoosterDao dao, int amount, long unitTypeId) {
        UnitType unitType = dao.getUnitTypeById(unitTypeId);

        for (int i = 0; i < amount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitTypeId);
            unit.setHealth(unitType.getHealth());
            dao.insert(unit);
        }
    }

    public void createUnits(RoosterDao dao, int amount, long unitTypeId, long buildingId) {
        UnitType unitType = dao.getUnitTypeById(unitTypeId);

        for (int i = 0; i < amount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitTypeId);
            unit.setHealth(unitType.getHealth());
            unit.setLocatedAtBuildingId(buildingId);
            dao.insert(unit);
        }
    }

    @Nullable
    protected UnitWithType getUnitWithTypeById(List<UnitWithType> unitsWithType, long id) {
        for (UnitWithType unitWithType : unitsWithType) {
            if (unitWithType.getUnit().getId() == id) {
                return unitWithType;
            }
        }
        return null;
    }

    @Nullable
    protected Unit getUnitById(List<Unit> units, long id) {
        for (Unit unit : units) {
            if (unit.getId() == id) {
                return unit;
            }
        }
        return null;
    }

    protected Unit createWoundedSoldier(RoosterDao dao) {
        return createWoundedSoldier(dao, 1).get(0);
    }

    protected List<Unit> createWoundedSoldier(RoosterDao dao, int amount) {
        // Create soldier.
        createUnitsJoiningUser(dao, amount, SOLDIER_UNIT_TYPE_ID);

        // Injure soldier.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        List<Unit> result = new ArrayList<>();
        assertEquals(amount, unitsWithType.size());
        for (int i = 0; i < amount; i++) {
            Unit soldier = unitsWithType.get(i).getUnit();
            soldier.setHealth(REMAINING_HEALTH);
            dao.update(soldier);
            result.add(soldier);
        }
        return result;
    }
}
