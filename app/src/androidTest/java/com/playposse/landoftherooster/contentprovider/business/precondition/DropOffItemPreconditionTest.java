package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link DropOffItemPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DropOffItemPreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_success_resource() {
        // Create wheat.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, null);

        // Create mill.
        long millId = createMillAndMarker(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, millId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_resourceNotJoiningUser() {
        // Create mill.
        long millId = createMillAndMarker(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, millId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_resourceNotUsedByBuilding() {
        // Create wheat.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, null);

        // Create wheat field.
        long wheatFieldId = createWheatField(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_success_unit() {
        // Create peasant.
        long peasantId = GameConfig.PEASANT_ID;
        RoosterDaoUtil.creditUnit(dao, peasantId, 1, null);

        // Create barracks.
        long barracksId = createBarracksAndMarker(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(barracksId, new UnitItem(peasantId));
        BusinessDataCache cache = new BusinessDataCache(dao, barracksId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_unitNotJoiningUser() {
        long peasantId = GameConfig.PEASANT_ID;

        // Create barracks.
        long barracksId = createBarracksAndMarker(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(barracksId, new UnitItem(peasantId));
        BusinessDataCache cache = new BusinessDataCache(dao, barracksId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_unitNotUsedByBuilding() {
        // Create peasant.
        long peasantId = GameConfig.PEASANT_ID;
        RoosterDaoUtil.creditUnit(dao, peasantId, 1, null);

        // Create wheat field.
        long wheatFieldId = createWheatField(dao);

        // Test precondition.
        DropOffItemEvent event =
                new DropOffItemEvent(wheatFieldId, new UnitItem(peasantId));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        DropOffItemPrecondition precondition = new DropOffItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result
        assertFalse(outcome.getSuccess());
    }
}
