package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link PickUpUnitFromHospitalPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpUnitFromHospitalPreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_success() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, SOLDIER_UNIT_TYPE_ID, hospitalId);
        Unit unit = units.get(0);

        // Test precondition.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(hospitalId);
        BusinessDataCache cache = new BusinessDataCache(dao, hospitalId);
        PickUpUnitFromHospitalPrecondition precondition = new PickUpUnitFromHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        PickUpUnitFromHospitalPreconditionOutcome castOutcome =
                (PickUpUnitFromHospitalPreconditionOutcome) outcome;
        assertTrue(outcome.getSuccess());
        assertNotNull(castOutcome.getUnitWithType());
        assertEquals(unit.getId(), castOutcome.getUnitWithType().getUnit().getId());
        Long locatedAtBuildingId = castOutcome.getUnitWithType().getUnit().getLocatedAtBuildingId();
        assertEquals((Long) hospitalId, locatedAtBuildingId);
    }

    @Test
    public void evaluate_fail_notAHealingBuilding() {
        // Create hospital.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, SOLDIER_UNIT_TYPE_ID, wheatFieldId);
        Unit unit = units.get(0);

        // Test precondition.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(wheatFieldId);
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        PickUpUnitFromHospitalPrecondition precondition = new PickUpUnitFromHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_fail_onlyInjuredUnit() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, SOLDIER_UNIT_TYPE_ID, hospitalId);
        Unit unit = units.get(0);

        // Injure unit.
        unit.setHealth(1);
        dao.update(unit);

        // Test precondition.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(hospitalId);
        BusinessDataCache cache = new BusinessDataCache(dao, hospitalId);
        PickUpUnitFromHospitalPrecondition precondition = new PickUpUnitFromHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_fail_onlyPeasantAsRecoveredUnit() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, GameConfig.PEASANT_ID, hospitalId);
        Unit unit = units.get(0);

        // Test precondition.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(hospitalId);
        BusinessDataCache cache = new BusinessDataCache(dao, hospitalId);
        PickUpUnitFromHospitalPrecondition precondition = new PickUpUnitFromHospitalPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }
}
