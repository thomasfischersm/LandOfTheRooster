package com.playposse.landoftherooster.contentprovider.business.initializer;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link ProductionStartupInitializer}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProductionStartupInitializerTest extends AbstractStartupInitializerTest {

    private static final String LOG_TAG =
            ProductionStartupInitializerTest.class.getSimpleName();

    @Test
    public void scheduleIfNecessary() throws InterruptedException {
        // Create mill.
        long millId = createMillAndMarker(dao);

        // Supply mill with wheat.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, millId);

        // Schedule production.
        long productionStartMs = System.currentTimeMillis() - GameConfig.PRODUCTION_CYCLE_MS;
        Building mill = dao.getBuildingById(millId);
        mill.setProductionStart(new Date(productionStartMs));
        dao.update(mill);

        // Start BusinessEngine.
        BusinessEngine.get()
                .start(targetContext);

        // Wait for schedule event to complete.
        waitForExecutedEventCount(2);

        // Assert that wheat has been produced.
        List<Resource> resources = dao.getResourcesByBuildingId(millId);
        assertEquals(1, resources.size());
        Resource resource = resources.get(0);
        assertEquals(FLOUR_RESOURCE_TYPE_ID, resource.getResourceTypeId());
        assertEquals(1, resource.getAmount());

        // Check MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(millId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 1, mapMarker.getCompletedProductionCount());
    }
}
