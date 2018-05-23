package com.playposse.landoftherooster;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.fail;

/**
 * A utility class to create test data.
 */
public class TestData {

    protected static final long CASTLE_BUILDING_TYPE_ID = 1;
    protected static final long WHEAT_FIELD_BUILDING_TYPE_ID = 2; // Mill: wheat (1) -> flour (2)
    protected static final long MILL_BUILDING_TYPE_ID = 3; // Mill: wheat (1) -> flour (2)
    protected static final long BAKERY_BUILDING_TYPE_ID = 4; // Mill: flour (2) -> bread (3)
    protected static final long VILLAGE_BUILDING_TYPE_ID = 5; // Village: bread (3) -> peasant (1)
    protected static final long BARRACKS_BUILDING_TYPE_ID = 6; // Barracks: peasant (1) -> soldier (2)
    protected static final long WHEAT_RESOURCE_TYPE_ID = 1;
    protected static final long FLOUR_RESOURCE_TYPE_ID = 2;
    protected static final long LATITUDE = 34;
    protected static final long LONGITUDE = 118;

    protected static long createWheatField(RoosterDao dao) {
        Building building = new Building(WHEAT_FIELD_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    protected static long createWheatFieldAndMarker(RoosterDao dao) {
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

    protected static long createMill(RoosterDao dao) {
        Building building = new Building(MILL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    protected static long createMillAndMarker(RoosterDao dao) {
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
    protected static long createBakery(RoosterDao dao) {
        Building building = new Building(BAKERY_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        return dao.insert(building);
    }

    protected void assertBuildingIds(
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

    protected void assertBuildingTypeIds(
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

    protected Date getLastModifiedForMarker(RoosterDao dao, long buildingId) {
        List<MapMarker> marker = dao.getMapMarkerByBuildingIds(Arrays.asList(buildingId));
        return marker.get(0).getLastModified();
    }
}
