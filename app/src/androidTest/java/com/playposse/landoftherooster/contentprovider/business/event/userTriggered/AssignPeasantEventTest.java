package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link AssignPeasantEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AssignPeasantEventTest extends AbstractBusinessTest {

    private static final String LOG_TAG = AssignPeasantEventTest.class.getSimpleName();

    private static final int ACCELERATED_PRODUCTION_CYCLE_MS = 450;

    @Test
    public void triggerEvent_noProduction() throws InterruptedException {
        // Speed up production for the test.
        int originalProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = ACCELERATED_PRODUCTION_CYCLE_MS;

        try {
            // Create building.
            long millId = createMillAndMarker(dao);

            // Assign peasant.
            RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);
            AssignPeasantEvent assignPeasantEvent = new AssignPeasantEvent(millId);
            BusinessEngine.get()
                    .triggerEvent(assignPeasantEvent);

            // Verify assignment.
            int peasantCount = dao.getUnitCount(GameConfig.PEASANT_ID, millId);
            assertEquals(1, peasantCount);

            // Check that nothing was produced.
            waitForExecutedEventCount(1);
            List<Resource> resources = dao.getResourcesByBuildingId(millId);
            assertEquals(0, resources.size());
        } finally {
            GameConfig.PRODUCTION_CYCLE_MS = originalProductionCycleMs;
        }
    }

    @Test
    public void triggerEvent_production() throws InterruptedException {
        // Speed up production for the test.
        int originalProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = ACCELERATED_PRODUCTION_CYCLE_MS;

        try {
            // Create building.
            long millId = createMillAndMarker(dao);

            // Start production.
            RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, millId);

            PostDropOffItemEvent postDropOffItemEvent =
                    new PostDropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
            BusinessEngine.get()
                    .triggerEvent(postDropOffItemEvent);

            // Assign peasant.
            RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);
            AssignPeasantEvent assignPeasantEvent = new AssignPeasantEvent(millId);
            BusinessEngine.get()
                    .triggerEvent(assignPeasantEvent);

            // Verify assignment.
            int peasantCount = dao.getUnitCount(GameConfig.PEASANT_ID, millId);
            assertEquals(1, peasantCount);

            // Check that production was restarted.
            waitForExecutedEventCount(5);
            List<Resource> resources = dao.getResourcesByBuildingId(millId);
            assertEquals(1, resources.size());
            Resource resource = resources.get(0);
            assertEquals(1, resource.getAmount());
            assertEquals(FLOUR_RESOURCE_TYPE_ID, resource.getResourceTypeId());
            assertEquals((Long) millId, resource.getLocatedAtBuildingId());
        } finally {
            GameConfig.PRODUCTION_CYCLE_MS = originalProductionCycleMs;
        }
    }

    @Test
    public void triggerEvent_freeProduction() throws InterruptedException {
        // Speed up production for the test.
        int originalProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = ACCELERATED_PRODUCTION_CYCLE_MS;

        try {
            // Create building.
            long wheatFieldId = createWheatFieldAndMarker(dao);

            // Start production.
            ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
            PostPickUpItemEvent postPickUpItemEvent =
                    new PostPickUpItemEvent(wheatFieldId, wheatItem);
            BusinessEngine.get()
                    .triggerEvent(postPickUpItemEvent);

            // Assign peasant.
            RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);
            AssignPeasantEvent assignPeasantEvent = new AssignPeasantEvent(wheatFieldId);
            BusinessEngine.get()
                    .triggerEvent(assignPeasantEvent);

            // Verify assignment.
            int peasantCount = dao.getUnitCount(GameConfig.PEASANT_ID, wheatFieldId);
            assertEquals(1, peasantCount);

            // Check that production was restarted.
            assertEquals(0, dao.getResourcesByBuildingId(wheatFieldId).size());
            waitForExecutedEventCount(5);

            List<Resource> resources = dao.getResourcesByBuildingId(wheatFieldId);
            assertEquals(1, resources.size());
            Resource resource = resources.get(0);
            assertEquals(1, resource.getAmount());
            assertEquals(WHEAT_RESOURCE_TYPE_ID, resource.getResourceTypeId());
            assertEquals((Long) wheatFieldId, resource.getLocatedAtBuildingId());
        } finally {
            GameConfig.PRODUCTION_CYCLE_MS = originalProductionCycleMs;
        }
    }
}
