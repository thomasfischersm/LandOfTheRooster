package com.playposse.landoftherooster.contentprovider.business;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.UserPicksUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for {@link BusinessEngine}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BusinessEngineTest extends AbstractBusinessTest {

    private static final String LOG_TAG = BusinessEngineTest.class.getSimpleName();

    private static final String ACTION_0 = "0";
    private static final String ACTION_1 = "1";
    private static final String ACTION_2 = "2";
    private static final long BUILDING_ID_0 = 0;
    private static final long DELAY_MS = 100;

    @Test
    public void registerAction() {
        final StringBuilder result = new StringBuilder();

        BusinessEvent event = new BusinessEvent(-1L);

        BusinessPrecondition successPrecondition = new BusinessPrecondition() {
            @Override
            public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
                return new PreconditionOutcome(true);
            }
        };

        BusinessAction action = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(ACTION_0);
            }
        };

        businessEngine.registerAction(event.getClass(), successPrecondition, action);
        businessEngine.triggerEvent(event);

        assertEquals("0", result.toString());
    }

    @Test
    public void triggerEvent_failedPrecondition() {
        final StringBuilder result = new StringBuilder();

        BusinessEvent event = new BusinessEvent(-1L);

        BusinessPrecondition successPrecondition = new BusinessPrecondition() {
            @Override
            public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
                return new PreconditionOutcome(false);
            }
        };

        BusinessAction action = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(ACTION_0);
            }
        };

        businessEngine.registerAction(event.getClass(), successPrecondition, action);
        businessEngine.triggerEvent(event);

        assertEquals("", result.toString());
    }

    /**
     * Triggers event 0. Event 0 will trigger a delayed event and an instant event. The test
     * assertion verifies that the events were executed in the right order.
     */
    @Test
    public void triggerDelayedEvent() {
        final StringBuilder result = new StringBuilder();

        BusinessEvent event0 = new BusinessEvent(-1L) {
        };
        final BusinessEvent event1 = new BusinessEvent(-1L) {
        };
        final BusinessEvent event2 = new BusinessEvent(-1L) {
        };

        BusinessPrecondition successPrecondition = new BusinessPrecondition() {
            @Override
            public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
                return new PreconditionOutcome(true);
            }
        };

        BusinessAction action0 = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                businessEngine.triggerDelayedEvent(event1);
                businessEngine.triggerEvent(event2);
                result.append(ACTION_0);
            }
        };

        BusinessAction action1 = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(ACTION_1);
            }
        };

        BusinessAction action2 = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(ACTION_2);
            }
        };

        businessEngine.registerAction(event0.getClass(), successPrecondition, action0);
        businessEngine.registerAction(event1.getClass(), successPrecondition, action1);
        businessEngine.registerAction(event2.getClass(), successPrecondition, action2);

        businessEngine.triggerEvent(event0);

        assertEquals("210", result.toString());
    }

    @Test
    public void scheduleEvent() throws InterruptedException {
        final StringBuilder result = new StringBuilder();

        BusinessEvent event = new BusinessEvent(-1L);

        BusinessPrecondition successPrecondition = new BusinessPrecondition() {
            @Override
            public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
                return new PreconditionOutcome(true);
            }
        };

        BusinessAction action = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(ACTION_0);
            }
        };

        businessEngine.registerAction(event.getClass(), successPrecondition, action);
        businessEngine.scheduleEvent(DELAY_MS, event);

        // Event should NOT yet have executed.
        assertEquals("", result.toString());

        Thread.sleep(2 * DELAY_MS);

        // Event should have executed by now.
        assertEquals("0", result.toString());
    }

    /**
     * Schedules an event and then preempts it by a second event for the same building.
     */
    @Test
    public void scheduleEvent_preemptEvent() throws InterruptedException {
        final StringBuilder result = new StringBuilder();

        BusinessEvent event0 = new BusinessEventWithTestMarker(BUILDING_ID_0, ACTION_0);
        BusinessEvent event1 = new BusinessEventWithTestMarker(BUILDING_ID_0, ACTION_1);

        BusinessPrecondition successPrecondition = new BusinessPrecondition() {
            @Override
            public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
                return new PreconditionOutcome(true);
            }
        };

        BusinessAction action = new BusinessAction() {
            @Override
            public void perform(
                    BusinessEvent event,
                    PreconditionOutcome preconditionOutcome,
                    BusinessDataCache dataCache) {

                result.append(event.toString());
                Log.d(LOG_TAG, "perform: Appended " + event.toString());
            }
        };

        businessEngine.registerAction(
                BusinessEventWithTestMarker.class,
                successPrecondition,
                action);
        businessEngine.scheduleEvent(2 * DELAY_MS, event0); // This should be canceled.
        businessEngine.scheduleEvent(DELAY_MS, event1);

        // Event should NOT yet have executed.
        assertEquals("", result.toString());

        Thread.sleep(3 * DELAY_MS);

        // Event should have executed by now.
        assertEquals("1", result.toString());
    }

    @Test
    public void triggerEvent_freeItemProduction() throws InterruptedException {
        // Temporarily set production cycle to near instantaneous.
        int savedProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = 10;

        // Create building.
        long buildingId = createWheatFieldAndMarker(dao);

        // Assert that input and output resources are 0.
        ResourceWithType outputResourceWithType =
                dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, buildingId);
        assertNull(outputResourceWithType);

        // Trigger building created event. The output resource should have been created instantly.
        BusinessEngine.get().triggerEvent(new BuildingCreatedEvent(buildingId));
        outputResourceWithType =
                dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, buildingId);
        assertNotNull(outputResourceWithType);
        assertEquals(1, outputResourceWithType.getResource().getAmount());
        assertEquals(
                WHEAT_RESOURCE_TYPE_ID,
                outputResourceWithType.getResource().getResourceTypeId());
        assertEquals(WHEAT_RESOURCE_TYPE_ID, outputResourceWithType.getType().getId());

        // Verify that the building production cycle hasn't started.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNull(buildingWithType.getBuilding().getProductionStart());

        // Pick up the produced item.
        outputResourceWithType.getResource().setAmount(0);
        dao.update(outputResourceWithType.getResource());

        // Fire item picked up event.
        ResourceItem item = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        BusinessEngine.get().triggerEvent(new UserPicksUpItemEvent(buildingId, item));

        // Wait for the production to complete.
        Thread.sleep(500);

        // Check that the resource was produced.
        outputResourceWithType =
                dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, buildingId);
        assertNotNull(outputResourceWithType);
        assertEquals(1, outputResourceWithType.getResource().getAmount());
        assertEquals(
                WHEAT_RESOURCE_TYPE_ID,
                outputResourceWithType.getResource().getResourceTypeId());
        assertEquals(WHEAT_RESOURCE_TYPE_ID, outputResourceWithType.getType().getId());

        // Reset the production cycle constant.
        GameConfig.PRODUCTION_CYCLE_MS = savedProductionCycleMs;

        // Fire item picked up event when the item has NOT been picked up.
        BusinessEngine.get().triggerEvent(new UserPicksUpItemEvent(buildingId, item));
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNull(buildingWithType.getBuilding().getProductionStart());

        // Pick up item and fire the relevant event.
        outputResourceWithType.getResource().setAmount(0);
        dao.update(outputResourceWithType.getResource());
        BusinessEngine.get().triggerEvent(new UserPicksUpItemEvent(buildingId, item));

        // Verify that the production has started but no resource has been produced.
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        outputResourceWithType =
                dao.getResourceWithType(WHEAT_RESOURCE_TYPE_ID, buildingId);
        assertNotNull(buildingWithType.getBuilding().getProductionStart());
        assertNull(outputResourceWithType);
    }

    /**
     * A {@link BusinessEvent} that returns a test marker in the {@link #toString()} method.
     */
    private static class BusinessEventWithTestMarker extends BusinessEvent {

        private final String action;

        private BusinessEventWithTestMarker(long buildingId, String action) {
            super(buildingId);

            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }
}
