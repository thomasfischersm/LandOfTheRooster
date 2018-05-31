package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * A test for {@link PickUpItemEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpItemEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_resource() {
        // Create mill.
        long millId = createMillAndMarker(dao);

        // Add flour to mill.
        RoosterDaoUtil.creditResource(dao, FLOUR_RESOURCE_TYPE_ID, 1, millId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(millId, new ResourceItem(FLOUR_RESOURCE_TYPE_ID));
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert the resource transferred.
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, FLOUR_RESOURCE_TYPE_ID, millId));
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, FLOUR_RESOURCE_TYPE_ID, null));

        // Assert that production has NOT started.
        Building resultBuilding = dao.getBuildingById(millId);
        assertNull(resultBuilding.getProductionStart());

        // Assert MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(millId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }

    @Test
    public void triggerEvent_freeResource() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add wheat to wheat field.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert the resource transferred.
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, wheatFieldId));
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));

        // Assert next free production has started.
        Building resultBuilding = dao.getBuildingById(wheatFieldId);
        assertNotNull(resultBuilding.getProductionStart());

        // Assert MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(wheatFieldId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 1, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }

    @Test
    public void triggerEvent_unit() {
        // Create barracks.
        long barracksId = createBarracksAndMarker(dao);

        // Add soldier to barracks.
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 1, barracksId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(barracksId, new UnitItem(SOLDIER_UNIT_TYPE_ID));
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert the outcome.
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, SOLDIER_UNIT_TYPE_ID, barracksId));
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, SOLDIER_UNIT_TYPE_ID, null));

        // Assert that production has NOT started.
        Building resultBuilding = dao.getBuildingById(barracksId);
        assertNull(resultBuilding.getProductionStart());

        // Assert MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(barracksId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }
}
