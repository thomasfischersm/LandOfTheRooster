package com.playposse.landoftherooster.contentprovider.business.event.other;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * An instrumented test for scenarios that are started by {@link LocationUpdateEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocationUpdateEventTest extends AbstractBusinessTest {

    private static final int FAIL_SAFE_MAX_ITERATION_COUNT = 5_000;

    @Test
    public void triggerEvent() {
        // Ensure that no buildings exist.
        assertEquals(0, dao.getAllBuildings().size());

        // Create castle.
        double lat = 34.0073;
        double lon = -118.486;
        LatLng latLng = new LatLng(lat, lon);
        BusinessEngine.get().triggerEvent(new LocationUpdateEvent(latLng));

        // Assert castle
        List<Building> buildings = dao.getAllBuildings();
        assertEquals(1, buildings.size());
        Building castle = buildings.get(0);
        assertEquals(CASTLE_BUILDING_TYPE_ID, castle.getBuildingTypeId());
    }

    @Test
    public void triggerEvent_freeProduction() {
        // Ensure that no buildings exist.
        assertEquals(0, dao.getAllBuildings().size());

        // Create buildings, including wheat field.
        double lat = 34.0073;
        double lon = -118.486;
        updateLocationUntilNewBuilding(lat, lon, 2);

        // Assert castle
        List<Building> buildings = dao.getAllBuildings();
        assertEquals(2, buildings.size());

        Building castle = buildings.get(0);
        assertEquals(CASTLE_BUILDING_TYPE_ID, castle.getBuildingTypeId());

        Building wheatField = buildings.get(1);
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, wheatField.getBuildingTypeId());

        MapMarker mapMarker = dao.getMapMarkerByBuildingId(wheatField.getId());
        assertTrue(mapMarker.isReady());
    }

    @Test
    public void triggerEvent_battleBuilding() {
        // Ensure that no buildings exist.
        assertEquals(0, dao.getAllBuildings().size());

        // Create buildings, including wheat field.
        double lat = 34.0073;
        double lon = -118.486;
        updateLocationUntilNewBuilding(lat, lon, 7);

        // Assert castle
        List<Building> buildings = dao.getAllBuildings();
        assertEquals(7, buildings.size());

        Building goblinCave = buildings.get(6);
        assertEquals(GOBLIN_CAVE_BUILDING_TYPE_ID, goblinCave.getBuildingTypeId());

        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCave.getId());
        assertTrue(mapMarker.isReady());
    }

    private void updateLocationUntilNewBuilding(double lat, double lon, int buildingCount) {
        for (int i = 0; i < FAIL_SAFE_MAX_ITERATION_COUNT; i++) {
            // Break out when enough buildings have been created.
            if (dao.getAllBuildings().size() >= buildingCount) {
                return;
            }

            LatLng latLng = new LatLng(lat, lon);
            BusinessEngine.get().triggerEvent(new LocationUpdateEvent(latLng));

            lat -= 0.0005;
        }
    }
}
