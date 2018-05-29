package com.playposse.landoftherooster.contentprovider.business.event;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link CompleteHealingEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CompleteHealingEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_singleUnit() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create injured soldier at hospital.
        Unit woundedSoldier = createWoundedSoldier(dao);
        UnitWithType unitWithType = dao.getUnitWithTypeJoiningUser(woundedSoldier.getId());

        woundedSoldier.setLocatedAtBuildingId(hospitalId);
        dao.update(woundedSoldier);

        // Set healing start.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        Building hospital = buildingWithType.getBuilding();
        int peasantCount = GameConfig.IMPLIED_PEASANT_COUNT;
        long healingTimeMs = unitWithType.getHealingTimeMs(peasantCount);
        long healingStarted = System.currentTimeMillis() - healingTimeMs;
        hospital.setHealingStarted(new Date(healingStarted));
        dao.update(hospital);

        // Trigger healing complete event.
        CompleteHealingEvent event = new CompleteHealingEvent(hospitalId);
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert that the soldier is healed.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        unitWithType = unitsWithType.get(0);
        assertFalse(unitWithType.isInjured());

        // Assert building state.
        buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        assertNull(buildingWithType.getBuilding().getHealingStarted());

        // Assert MapMarker state.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 1, mapMarker.getCompletedProductionCount());
    }

    @Test
    public void triggerEvent_twoUnits() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create injured soldier at hospital.
        List<Unit> woundedSoldiers = createWoundedSoldier(dao, 2);
        Unit woundedSoldier0 = woundedSoldiers.get(0);
        Unit woundedSoldier1 = woundedSoldiers.get(1);
        UnitWithType unitWithType0 = dao.getUnitWithTypeJoiningUser(woundedSoldier0.getId());

        woundedSoldier0.setLocatedAtBuildingId(hospitalId);
        // Create a dependable sequence of healing by having differently injured units.
        woundedSoldier0.setHealth(2 * REMAINING_HEALTH);
        dao.update(woundedSoldier0);

        woundedSoldier1.setLocatedAtBuildingId(hospitalId);
        dao.update(woundedSoldier1);

        // Set healing start.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        Building hospital = buildingWithType.getBuilding();
        int peasantCount = GameConfig.IMPLIED_PEASANT_COUNT;
        long healingTimeMs = unitWithType0.getHealingTimeMs(peasantCount);
        long healingStarted = System.currentTimeMillis() - healingTimeMs;
        hospital.setHealingStarted(new Date(healingStarted));
        dao.update(hospital);

        // Trigger healing complete event.
        CompleteHealingEvent event = new CompleteHealingEvent(hospitalId);
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert that the first soldier is healed.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        unitWithType0 = getUnitWithTypeById(unitsWithType, woundedSoldier0.getId());
        assertFalse(unitWithType0.isInjured());

        // Assert that second soldier is still wounded.
        UnitWithType unitWithType1 = getUnitWithTypeById(unitsWithType, woundedSoldier1.getId());
        assertTrue(unitWithType1.isInjured());

        // Assert building state.
        buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        assertNotNull(buildingWithType.getBuilding().getHealingStarted());

        // Assert MapMarker state.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 1, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 1, mapMarker.getCompletedProductionCount());

        // SECOND HEALING EVENT!
        // Set healing start.
        buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        hospital = buildingWithType.getBuilding();
        hospital.setHealingStarted(new Date(healingStarted));
        dao.update(hospital);

        // Trigger healing complete event.
        event = new CompleteHealingEvent(hospitalId);
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert that the first soldier is healed.
        unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        unitWithType0 = getUnitWithTypeById(unitsWithType, woundedSoldier0.getId());
        assertFalse(unitWithType0.isInjured());

        // Assert that second soldier is still wounded.
        unitWithType1 = getUnitWithTypeById(unitsWithType, woundedSoldier1.getId());
        assertFalse(unitWithType1.isInjured());

        // Assert building state.
        buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        assertNull(buildingWithType.getBuilding().getHealingStarted());

        // Assert MapMarker state.
        mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 2, mapMarker.getCompletedProductionCount());
    }
}
