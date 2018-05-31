package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link PickUpItemPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpItemPreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_success() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add wheat to wheat field.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        PickUpItemPrecondition precondition = new PickUpItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert the outcome.
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_noCarryCapacity() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add wheat to wheat field.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);

        // Fill up carry capacity.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, null);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        PickUpItemPrecondition precondition = new PickUpItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert the outcome.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failure_noItemAtBuilding() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        PickUpItemPrecondition precondition = new PickUpItemPrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert the outcome.
        assertFalse(outcome.getSuccess());
    }
}
