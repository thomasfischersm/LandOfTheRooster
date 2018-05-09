package com.playposse.landoftherooster.contentprovider.room;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitCountByType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * A test for {@link RoosterDao}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoosterDaoTest {

    private static final String LOG_TAG = RoosterDaoTest.class.getSimpleName();

    private static final int RESOURCE_TYPE_ID = 1;
    private static final int RESOURCE_AMOUNT = 123;

    private RoosterDao dao;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();
    }

    @Test
    public void simpleDatabaseAccess() {
        // Insert resource.
        Resource resource = new Resource(RESOURCE_TYPE_ID, RESOURCE_AMOUNT, null);
        long resourceId = dao.insert(resource);
        Log.i(LOG_TAG, "simpleDatabaseAccess: Inserted a resource with id: " + resourceId);

        // Retrieve the resource.
        ResourceWithType resourceWithType = dao.getResourceWithTypeJoiningUser(1);

        assertNotNull(resourceWithType);
        assertEquals(RESOURCE_AMOUNT, resourceWithType.getResource().getAmount());
    }

    @Test
    public void getResourceCountJoiningUser() {
        int count = dao.getResourceCountJoiningUser();
        assertEquals(0, count);

        dao.insert(new Resource(1, 1, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(1, count);

        dao.insert(new Resource(2, 2, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(3, count);

        dao.insert(new Resource(2, 3, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(6, count);
    }

    @Test
    public void getUnitCountsJoiningUser() throws InterruptedException {
        // Wait for the test data to be finished creating. Then delete the test units.
        Thread.sleep(1_000);
        dao.deleteUnits();

        List<UnitCountByType> unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(0, unitCountList.size());

        dao.insert(new Unit(1, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(1, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());

        dao.insert(new Unit(2, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(1, unitCountList.get(1).getCount());

        dao.insert(new Unit(2, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(2, unitCountList.get(1).getCount());

        dao.insert(new Unit(1, 10, 1L));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(2, unitCountList.get(1).getCount());
    }

    @Test
    public void getCarryingCapacity() throws InterruptedException {
        // Wait for the test data to be finished creating. Then delete the test units.
        Thread.sleep(1_000);
        dao.deleteUnits();

        assertEquals(0, dao.getCarryingCapacity());

        // Add peasant.
        dao.insert(new Unit(1, 10, null));
        assertEquals(1, dao.getCarryingCapacity());

        // Add soldier.
        dao.insert(new Unit(2, 10, null));
        assertEquals(1, dao.getCarryingCapacity());

        // Add peasant to building.
        dao.insert(new Unit(1, 10, 1L));
        assertEquals(1, dao.getCarryingCapacity());

        // Add second peasant.
        dao.insert(new Unit(1, 10, null));
        assertEquals(2, dao.getCarryingCapacity());
    }
}
