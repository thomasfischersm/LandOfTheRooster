package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
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

/**
 * A test for {@link DropOffItemEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DropOffItemEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_resource() {
        // Create wheat.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, null);

        // Create mill.
        long millId = createMillAndMarker(dao);

        // Assert original state.
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, millId));

        // Test action.
        DropOffItemEvent event =
                new DropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert item transferred.
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, millId));

        // Assert production started.
        Building resultBuilding = dao.getBuildingById(millId);
        assertNotNull(resultBuilding.getProductionStart());

        // Assert MapMarker updated.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(millId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 1, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }


    @Test
    public void triggerEvent_unit() {
        // Create peasant.
        long peasantId = GameConfig.PEASANT_ID;
        RoosterDaoUtil.creditUnit(dao, peasantId, 1, null);

        // Create mill.
        long barracksId = createBarracksAndMarker(dao);

        // Assert original state.
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, null));
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, barracksId));

        // Test action.
        DropOffItemEvent event =
                new DropOffItemEvent(barracksId, new UnitItem(peasantId));
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert unit transferred.
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, null));
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, barracksId));

        // Assert production started.
        Building resultBuilding = dao.getBuildingById(barracksId);
        assertNotNull(resultBuilding.getProductionStart());

        // Assert MapMarker updated.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(barracksId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 1, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }
}
