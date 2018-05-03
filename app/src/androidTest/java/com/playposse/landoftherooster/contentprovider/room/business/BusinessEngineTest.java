package com.playposse.landoftherooster.contentprovider.room.business;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for {@link BusinessEngine}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BusinessEngineTest {

    private static final long BUILDING_TYPE_ID = 3; // Mill: wheat (1) -> flour (2)
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
        long buildingId = dao.insert(new Building(BUILDING_TYPE_ID, LATITUDE, LONGITUDE));

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
