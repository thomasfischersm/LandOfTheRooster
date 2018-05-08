package com.playposse.landoftherooster.contentprovider.room.business;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserPicksUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

import org.junit.Before;
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
public class BusinessEngineTest {

    private static final long WHEAT_FIELD_BUILDING_TYPE_ID = 2; // Mill: wheat (1) -> flour (2)
    private static final long MILL_BUILDING_TYPE_ID = 3; // Mill: wheat (1) -> flour (2)
    private static final long WHEAT_RESOURCE_TYPE_ID = 1;
    private static final long FLOUR_RESOURCE_TYPE_ID = 2;
    private static final long LATITUDE = 34;
    private static final long LONGITUDE = 118;

    private RoosterDao dao;
    private BusinessEngine businessEngine;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Start business engine.
        businessEngine = BusinessEngine.get();
        businessEngine.start(targetContext);

        // Clear data that test may generate.
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();
    }

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

    @Test
    public void triggerEvent_CompleteItemProductionEvent() throws InterruptedException {
        // Temporarily set production cycle to near instantaneous.
        int savedProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = 10;

        // Create building.
        long buildingId = dao.insert(new Building(MILL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE));

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

    @Test
    public void triggerEvent_freeItemProduction() throws InterruptedException {
        // Temporarily set production cycle to near instantaneous.
        int savedProductionCycleMs = GameConfig.PRODUCTION_CYCLE_MS;
        GameConfig.PRODUCTION_CYCLE_MS = 10;

        // Create building.
        long buildingId =
                dao.insert(new Building(WHEAT_FIELD_BUILDING_TYPE_ID, LATITUDE, LONGITUDE));

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
}
