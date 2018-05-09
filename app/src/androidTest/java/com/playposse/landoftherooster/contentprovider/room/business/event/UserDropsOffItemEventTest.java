package com.playposse.landoftherooster.contentprovider.room.business.event;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEvent;
import com.playposse.landoftherooster.contentprovider.room.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for scenarios that are started by {@link UserDropsOffItemEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserDropsOffItemEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_UserDropsOffItemEvent() {
        // Create building.
        long buildingId = dao.insert(new Building(MILL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE));

        // Drop off an unrelated resource.
        businessEngine.triggerEvent(
                UserDropsOffItemEvent.createForResource(buildingId, 1));
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNull(buildingWithType.getBuilding().getProductionStart());

        // Drop off prerequisite.
        dao.insert(new Resource(WHEAT_RESOURCE_TYPE_ID, 1, buildingId));
        businessEngine.triggerEvent(
                UserDropsOffItemEvent.createForResource(buildingId, WHEAT_RESOURCE_TYPE_ID));
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNotNull(buildingWithType.getBuilding().getProductionStart());
    }
}
