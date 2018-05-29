package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.util.Log;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.UnitInjuredEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link AdmitUnitToHospitalEvent}.
 */
public class AdmitUnitToHospitalEventTest extends AbstractBusinessTest {

    private static final String LOG_TAG = AdmitUnitToHospitalEventTest.class.getSimpleName();

    private static final int REMAINING_HEALTH = 1;

    @Test
    public void triggerEvent_normal() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create wounded soldier.
        Unit soldier = createWoundedSoldier(dao);

        // Trigger event.
        AdmitUnitToHospitalEvent event = new AdmitUnitToHospitalEvent(hospitalId, soldier.getId());
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert result.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        assertEquals(1, unitsWithType.size());

        Unit resultSoldier = unitsWithType.get(0).getUnit();
        assertEquals(soldier.getId(), resultSoldier.getId());
        assertEquals(soldier.getHealth(), resultSoldier.getHealth());
        assertEquals((Long) hospitalId, resultSoldier.getLocatedAtBuildingId());
    }

    @Test
    public void triggerEvent_multiple() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create three wounded soldier.
        List<Unit> soldiers = createWoundedSoldier(dao,3);
        Unit soldier0 = soldiers.get(0);
        Unit soldier1 = soldiers.get(1);
        Unit soldier2 = soldiers.get(2);

        // Trigger first event.
        AdmitUnitToHospitalEvent event = new AdmitUnitToHospitalEvent(hospitalId, soldier0.getId());
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert result.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        assertEquals(1, unitsWithType.size());

        Unit resultSoldier = unitsWithType.get(0).getUnit();
        assertEquals(soldier0.getId(), resultSoldier.getId());
        assertEquals(soldier0.getHealth(), resultSoldier.getHealth());
        assertEquals((Long) hospitalId, resultSoldier.getLocatedAtBuildingId());

        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 1, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());

        // Trigger second event.
        event = new AdmitUnitToHospitalEvent(hospitalId, soldier1.getId());
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert result.
        unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        assertEquals(2, unitsWithType.size());

        resultSoldier = getUnitWithTypeById(unitsWithType, soldier1.getId()).getUnit();
        assertEquals(soldier1.getId(), resultSoldier.getId());
        assertEquals(soldier1.getHealth(), resultSoldier.getHealth());
        assertEquals((Long) hospitalId, resultSoldier.getLocatedAtBuildingId());

        mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 2, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());

        // Trigger third event.
        event = new AdmitUnitToHospitalEvent(hospitalId, soldier2.getId());
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert result.
        unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        assertEquals(3, unitsWithType.size());

        resultSoldier = getUnitWithTypeById(unitsWithType, soldier2.getId()).getUnit();
        assertEquals(soldier2.getId(), resultSoldier.getId());
        assertEquals(soldier2.getHealth(), resultSoldier.getHealth());
        assertEquals((Long) hospitalId, resultSoldier.getLocatedAtBuildingId());

        mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        // Ensure that marker is false when the last unit is admitted.
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 3, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }

    @Test
    public void triggerEvent_ensureProductionCompletes() throws InterruptedException {
        // Set healing speed to instant.
        int originalHealingRate = GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS;
        GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS = 0;

        try {
            // Create hospital.
            long hospitalId = createHospitalAndMarker(dao);

            // Create wounded soldier.
            Unit soldier = createWoundedSoldier(dao);

            // Trigger event.
            AdmitUnitToHospitalEvent event = new AdmitUnitToHospitalEvent(hospitalId, soldier.getId());
            BusinessEngine.get()
                    .triggerEvent(event);

            // Wait for healing to complete.
            Thread.sleep(60);

            // Assert result.
            List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
            assertEquals(1, unitsWithType.size());

            UnitWithType resultSolderWithType = unitsWithType.get(0);
            Unit resultSoldier = resultSolderWithType.getUnit();
            assertEquals(soldier.getId(), resultSoldier.getId());
            assertFalse(resultSolderWithType.isInjured());
            assertEquals((Long) hospitalId, resultSoldier.getLocatedAtBuildingId());
        } finally {
            // Restore original healing rate.
            GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS = originalHealingRate;
        }
    }

    @Test
    public void triggerEvent_completeCycleTwoUnits() throws InterruptedException {
        // Set healing speed to instant.
        int originalHealingRate = GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS;
        GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS = 500;

        try {
            // Create 2 hospitals.
            long hospitalId = createHospitalAndMarker(dao);
            long otherHospitalId = createHospitalAndMarker(dao);

            // Create wounded soldier.
            List<Unit> woundedSoldiers = createWoundedSoldier(dao, 2);
            Unit soldier0 = woundedSoldiers.get(0);
            Unit soldier1 = woundedSoldiers.get(1);

            // Wound soldier 0 more to ensure a stable healing order.
            UnitType soldierType = dao.getUnitTypeById(SOLDIER_UNIT_TYPE_ID);
            soldier0.setHealth(soldierType.getHealth() - 1);
            dao.update(soldier0);

            soldier1.setHealth(soldierType.getHealth() - 2);
            dao.update(soldier1);

            // TRIGGER EVENT for wounded soldier to get the hospital into the right initial state.
            BusinessEngine.get()
                    .triggerEvent(new UnitInjuredEvent());

            // Assert that both hospitals are ready to pickup wounded soldiers.
            assertMapMarker(
                    hospitalId,
                    true,
                    0,
                    0);
            assertMapMarker(
                    otherHospitalId,
                    true,
                    0,
                    0);

            // TRIGGER EVENT to admit first soldier.
            long start = System.currentTimeMillis();
            AdmitUnitToHospitalEvent event =
                    new AdmitUnitToHospitalEvent(hospitalId, soldier0.getId());
            BusinessEngine.get()
                    .triggerEvent(event);
            long middle = System.currentTimeMillis();
            Log.i(LOG_TAG, "triggerEvent_completeCycleTwoUnits: YOUDURATION " + (middle - start));

            // Assert result.
            List<Unit> units = dao.getUnits(SOLDIER_UNIT_TYPE_ID, hospitalId);
            Log.i(LOG_TAG, "triggerEvent_completeCycleTwoUnits: TWO " + (System.currentTimeMillis() - middle));
            assertEquals(1, units.size());

            Unit resultSoldier0 = units.get(0);
            UnitWithType resultSoldierWithType0 = new UnitWithType(resultSoldier0, soldierType);
            assertEquals(soldier0.getId(), resultSoldier0.getId());
            assertTrue(resultSoldierWithType0.isInjured());
            assertEquals((Long) hospitalId, resultSoldier0.getLocatedAtBuildingId());

            // Assert MapMarker for both hospitals
            assertMapMarker(
                    hospitalId,
                    true,
                    1,
                    0);
            assertMapMarker(
                    otherHospitalId,
                    true,
                    0,
                    0);

            // TRIGGER EVENT to admit second soldier.
            event = new AdmitUnitToHospitalEvent(hospitalId, soldier1.getId());
            BusinessEngine.get()
                    .triggerEvent(event);

            // Assert result.
            units = dao.getUnits(SOLDIER_UNIT_TYPE_ID, hospitalId);
            assertEquals(2, units.size());

            resultSoldier0 = getUnitById(units, soldier0.getId());
            resultSoldierWithType0 = new UnitWithType(resultSoldier0, soldierType);
            long end = System.currentTimeMillis();
            Log.i(LOG_TAG, "triggerEvent_completeCycleTwoUnits: MYDURATION: " + (end - start)); if (1== 1) return;
            assertEquals(soldier0.getId(), resultSoldier0.getId());
            assertTrue(resultSoldierWithType0.isInjured());
            assertEquals((Long) hospitalId, resultSoldier0.getLocatedAtBuildingId());

            Unit resultSoldier1 = getUnitById(units, soldier1.getId());
            UnitWithType resultSoldierWithType1 = new UnitWithType(resultSoldier1, soldierType);
            assertEquals(soldier1.getId(), resultSoldier1.getId());
            assertTrue(resultSoldierWithType1.isInjured());
            assertEquals((Long) hospitalId, resultSoldier1.getLocatedAtBuildingId());

            // Assert MapMarker for both hospitals
            assertMapMarker(
                    hospitalId,
                    false,
                    2,
                    0);
            assertMapMarker(
                    otherHospitalId,
                    false,
                    0,
                    0);

            // Wait for healing to occur of the first soldier.
            Thread.sleep(520);

            // Assert result.
            units = dao.getUnits(SOLDIER_UNIT_TYPE_ID, hospitalId);
            assertEquals(2, units.size());

            resultSoldier0 = getUnitById(units, soldier0.getId());
            resultSoldierWithType0 = new UnitWithType(resultSoldier0, soldierType);
            assertEquals(soldier0.getId(), resultSoldier0.getId());
            assertFalse(resultSoldierWithType0.isInjured());
            assertEquals((Long) hospitalId, resultSoldier0.getLocatedAtBuildingId());

            resultSoldier1 = getUnitById(units, soldier1.getId());
            resultSoldierWithType1 = new UnitWithType(resultSoldier1, soldierType);
            assertEquals(soldier1.getId(), resultSoldier1.getId());
            assertTrue(resultSoldierWithType1.isInjured());
            assertEquals((Long) hospitalId, resultSoldier1.getLocatedAtBuildingId());

            // Assert MapMarker for both hospitals
            assertMapMarker(
                    hospitalId,
                    true,
                    1,
                    1);
            assertMapMarker(
                    otherHospitalId,
                    false,
                    0,
                    0);

            // Wait for healing to occur of the second soldier.
            Thread.sleep(1_020);

            // Assert result.
            units = dao.getUnits(SOLDIER_UNIT_TYPE_ID, hospitalId);
            assertEquals(2, units.size());

            resultSoldier0 = getUnitById(units, soldier0.getId());
            resultSoldierWithType0 = new UnitWithType(resultSoldier0, soldierType);
            assertEquals(soldier0.getId(), resultSoldier0.getId());
            assertFalse(resultSoldierWithType0.isInjured());
            assertEquals((Long) hospitalId, resultSoldier0.getLocatedAtBuildingId());

            resultSoldier1 = getUnitById(units, soldier1.getId());
            resultSoldierWithType1 = new UnitWithType(resultSoldier1, soldierType);
            assertEquals(soldier1.getId(), resultSoldier1.getId());
            assertFalse(resultSoldierWithType1.isInjured());
            assertEquals((Long) hospitalId, resultSoldier1.getLocatedAtBuildingId());

            // Assert MapMarker for both hospitals
            assertMapMarker(
                    hospitalId,
                    true,
                    0,
                    2);
            assertMapMarker(
                    otherHospitalId,
                    false,
                    0,
                    0);

            // TODO: Pickup both units. (Add barracks to see them turn green.)
        } finally {
            // Restore original healing rate.
            GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS = originalHealingRate;
        }
    }

    private void assertMapMarker(
            long hospitalId,
            boolean expectedIsReady,
            int expectedPendingProductionCount,
            int expectedCompletedProductionCount) {

        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertEquals(expectedIsReady, mapMarker.isReady());
        assertEquals(
                (Integer) expectedPendingProductionCount,
                mapMarker.getPendingProductionCount());
        assertEquals(
                (Integer) expectedCompletedProductionCount,
                mapMarker.getCompletedProductionCount());
    }

}
