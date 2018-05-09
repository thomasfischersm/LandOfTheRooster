package com.playposse.landoftherooster.contentprovider.room.business.event;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.LocationUpdateEvent;
import com.playposse.landoftherooster.contentprovider.room.business.BusinessEngineTest;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * An instrumented test for scenarios that are started by {@link LocationUpdateEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocationUpdateEventTest extends BusinessEngineTest {

    @Test
    public void triggerEvent_LocationUpdateEvent() {
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
}
