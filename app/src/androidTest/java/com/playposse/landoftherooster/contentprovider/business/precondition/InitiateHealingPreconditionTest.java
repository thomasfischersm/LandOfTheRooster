package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.InitiateHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link InitiateHealingPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitiateHealingPreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_successful() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create injured unit at the hospital.
        Unit woundedSoldier = createWoundedSoldier(dao);
        woundedSoldier.setLocatedAtBuildingId(hospitalId);
        dao.update(woundedSoldier);

        // Test precondition.
        PreconditionOutcome outcome = executePrecondition(hospitalId);

        // Assert result.
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_healingAlreadyStarted() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Start healing already.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);
        Building building = buildingWithType.getBuilding();
        building.setHealingStarted(new Date());
        dao.update(building);

        // Create injured unit at the hospital.
        Unit woundedSoldier = createWoundedSoldier(dao);
        woundedSoldier.setLocatedAtBuildingId(hospitalId);
        dao.update(woundedSoldier);

        // Test precondition.
        PreconditionOutcome outcome = executePrecondition(hospitalId);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_notAHealingBuilding() {
        // Create hospital.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Create injured unit at the hospital.
        Unit woundedSoldier = createWoundedSoldier(dao);
        woundedSoldier.setLocatedAtBuildingId(wheatFieldId);
        dao.update(woundedSoldier);

        // Test precondition.
        PreconditionOutcome outcome = executePrecondition(wheatFieldId);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_buildingHasNoInjuredUnit() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create injured unit with user instead of hospital.
        Unit woundedSoldier = createWoundedSoldier(dao);

        // Test precondition.
        PreconditionOutcome outcome = executePrecondition(hospitalId);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    private PreconditionOutcome executePrecondition(long hospitalId) {
        InitiateHealingEvent event = new InitiateHealingEvent(hospitalId);
        BusinessDataCache cache = new BusinessDataCache(dao, hospitalId);
        InitiateHealingPrecondition precondition = new InitiateHealingPrecondition();
        return precondition.evaluate(event, cache);
    }
}
