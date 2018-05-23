package com.playposse.landoftherooster.contentprovider.business.event;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for scenarios that are started by {@link ItemProductionEndedEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItemProductionEndedEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_ItemProductionEndedEvent() throws InterruptedException {
        // Temporarily set production cycle to near instantaneous.
        int savedProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = 10;

        // Create building.
        long buildingId = createMillAndMarker(dao);

        // Drop off prerequisite.
        dao.insert(new Resource(WHEAT_RESOURCE_TYPE_ID, 1, buildingId));
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        businessEngine.triggerEvent(
                UserDropsOffItemEvent.createForResource(buildingId, WHEAT_RESOURCE_TYPE_ID));
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNotNull(buildingWithType.getBuilding().getProductionStart());

        // Wait for the production to complete.
        Thread.sleep(500);

        // Check that the prerequisite (wheat) is consumed.
        ResourceWithType inputResourceWithType =
                dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, buildingId);
        assertNull(inputResourceWithType);

        // Check that the output (flour) has been created.
        ResourceWithType outputResourceWithType =
                dao.getResourceWithType(FLOUR_RESOURCE_TYPE_ID, buildingId);
        assertNotNull(outputResourceWithType);
        assertEquals(1, outputResourceWithType.getResource().getAmount());

        // Reset the production cycle constant.
        GameConfig.PRODUCTION_CYCLE_MS = savedProductionCycleMs;
    }
}
