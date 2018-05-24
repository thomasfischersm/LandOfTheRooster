package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.AssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link AssignPeasantPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AssignPeasantPreconditionTest extends AbstractBusinessTest{

    @Test
    public void evaluate_success() {
        // Create building.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add peasant joining user.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);

        // Evaluate precondition.
        AssignPeasantEvent event = new AssignPeasantEvent(wheatFieldId);
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        AssignPeasantPrecondition precondition = new AssignPeasantPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert success.
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_tooManyPeasantsAssigned() {
        // Create building.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add peasant joining user.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);

        // Add 4 peasants to the building to fill it up.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 4, wheatFieldId);

        // Evaluate precondition.
        AssignPeasantEvent event = new AssignPeasantEvent(wheatFieldId);
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        AssignPeasantPrecondition precondition = new AssignPeasantPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert success.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_noPeasantJoiningUser() {
        // Create building.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Evaluate precondition.
        AssignPeasantEvent event = new AssignPeasantEvent(wheatFieldId);
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        AssignPeasantPrecondition precondition = new AssignPeasantPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert success.
        assertFalse(outcome.getSuccess());
    }
}
