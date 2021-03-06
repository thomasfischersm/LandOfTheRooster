package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for scenarios that are started by {@link CompleteProductionEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CompleteProductionEventTest extends AbstractBusinessTest {

    private static final String LOG_TAG = CompleteProductionEventTest.class.getSimpleName();

    @Test
    public void triggerEvent_ItemProductionEndedEvent() throws InterruptedException {
        // Temporarily set production cycle to near instantaneous.
        int savedProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = 60;

        try {
            // Create building.
            long millId = createMillAndMarker(dao);

            // Drop off prerequisite.
            dao.insert(new Resource(WHEAT_RESOURCE_TYPE_ID, 1, millId));
            BuildingRepository.stop();
            businessEngine.triggerEvent(
                    new PostDropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID)));
            BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(millId);
            assertNotNull(buildingWithType.getBuilding().getProductionStart());

            // Wait for the production to complete.
            waitForExecutedEventCount(3);

            // Check that the prerequisite (wheat) is consumed.
            ResourceWithType inputResourceWithType =
                    dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, millId);
            assertNull(inputResourceWithType);

            // Check that the output (flour) has been created.
            ResourceWithType outputResourceWithType =
                    dao.getResourceWithType(FLOUR_RESOURCE_TYPE_ID, millId);
            assertNotNull(outputResourceWithType);
            assertEquals(1, outputResourceWithType.getResource().getAmount());
        } finally {
            // Reset the production cycle constant.
            GameConfig.PRODUCTION_CYCLE_MS = savedProductionCycleMs;
        }
    }
}
