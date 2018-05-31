package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link AdmitUnitToHospitalPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdmitUnitToHospitalPreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_success() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create unit.
        createUnitsJoiningUser(dao, 1, SOLDIER_UNIT_TYPE_ID);

        // Injure unit.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        assertEquals(1, unitsWithType.size());
        Unit soldier = unitsWithType.get(0).getUnit();
        soldier.setHealth(REMAINING_HEALTH);
        dao.update(soldier);

        // Evaluate precondition.
        AdmitUnitToHospitalEvent event = new AdmitUnitToHospitalEvent(hospitalId, soldier.getId());
        BusinessDataCache dataCache = new BusinessDataCache(dao, hospitalId);
        AdmitUnitToHospitalPrecondition precondition = new AdmitUnitToHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, dataCache);
        AdmitUnitToHospitalPreconditionOutcome castOutcome =
                (AdmitUnitToHospitalPreconditionOutcome) outcome;

        // Assert result.
        assertTrue(castOutcome.getSuccess());

        Unit resultUnit = castOutcome.getUnitWithType().getUnit();
        assertEquals(soldier.getId(), resultUnit.getId());
        assertEquals(REMAINING_HEALTH, resultUnit.getHealth());
        assertEquals(SOLDIER_UNIT_TYPE_ID, resultUnit.getUnitTypeId());

        UnitType resultType = castOutcome.getUnitWithType().getType();
        assertEquals(SOLDIER_UNIT_TYPE_ID, resultType.getId());
    }

    @Test
    public void evaluate_failure_notAHealingBuilding() {
        // Create non-hospital
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Create unit.
        createUnitsJoiningUser(dao, 1, SOLDIER_UNIT_TYPE_ID);

        // Injure unit.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        assertEquals(1, unitsWithType.size());
        Unit soldier = unitsWithType.get(0).getUnit();
        soldier.setHealth(REMAINING_HEALTH);
        dao.update(soldier);

        // Evaluate precondition.
        AdmitUnitToHospitalEvent event =
                new AdmitUnitToHospitalEvent(wheatFieldId, soldier.getId());
        BusinessDataCache dataCache = new BusinessDataCache(dao, wheatFieldId);
        AdmitUnitToHospitalPrecondition precondition = new AdmitUnitToHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, dataCache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_success_unitNotInjured() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create unit and leave fully healthy.
        createUnitsJoiningUser(dao, 1, SOLDIER_UNIT_TYPE_ID);
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        assertEquals(1, unitsWithType.size());
        Unit soldier = unitsWithType.get(0).getUnit();

        // Evaluate precondition.
        AdmitUnitToHospitalEvent event = new AdmitUnitToHospitalEvent(hospitalId, soldier.getId());
        BusinessDataCache dataCache = new BusinessDataCache(dao, hospitalId);
        AdmitUnitToHospitalPrecondition precondition = new AdmitUnitToHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, dataCache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }
}
