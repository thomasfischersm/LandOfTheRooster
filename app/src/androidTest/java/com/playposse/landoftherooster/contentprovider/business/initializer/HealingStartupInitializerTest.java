package com.playposse.landoftherooster.contentprovider.business.initializer;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.TestDataWithDao;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link HealingStartupInitializer}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HealingStartupInitializerTest extends TestDataWithDao {

    @Before
    public void setUp2() {
        // Ensure that BusinessEngine hasn't been initialized yet.
        BusinessEngine.get()
                .stop();
    }

    @Test
    public void scheduleIfNecessary() throws InterruptedException {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Start healing time.
        Building hospital = dao.getBuildingById(hospitalId);
        long healingStartedMs =
                System.currentTimeMillis() - GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS;
        hospital.setHealingStarted(new Date(healingStartedMs));
        dao.update(hospital);

        // Add soldier to hospital.
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 1, hospitalId);

        // Wound soldier.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        UnitWithType unitWithType = unitsWithType.get(0);
        Unit unit = unitWithType.getUnit();
        unit.setHealth(unit.getHealth() - 1);
        dao.update(unit);

        // Start BusinessEngine.
        BusinessEngine.get()
                .start(targetContext);

        // Wait for schedule event to complete.
        waitForExecutedEventCount(2);

        // Assert that the soldier is healed.
        List<UnitWithType> resultUnitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        UnitWithType resultUnitWithType = resultUnitsWithType.get(0);
        assertFalse(resultUnitWithType.isInjured());

        // Assert the MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 1, mapMarker.getCompletedProductionCount());
    }

    @After
    public void tearDown() {
        BusinessEngine.get().stop();
        BuildingDiscoveryRepository.get(dao).reset();
    }
}
