package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link PickUpItemAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpItemActionTest extends AbstractBusinessTest {

    @Test
    public void perform_resource() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Add wheat to wheat field.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(wheatFieldId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        PreconditionOutcome outcome = new PreconditionOutcome(true);
        PickUpItemAction action = new PickUpItemAction();
        action.perform(event, outcome, cache);

        // Assert the outcome.
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, wheatFieldId));
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));
    }

    @Test
    public void perform_unit() {
        // Create barracks.
        long barracksId = createBarracksAndMarker(dao);

        // Add soldier to barracks.
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 1, barracksId);

        // Test precondition.
        PickUpItemEvent event =
                new PickUpItemEvent(barracksId, new UnitItem(SOLDIER_UNIT_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, barracksId);
        PreconditionOutcome outcome = new PreconditionOutcome(true);
        PickUpItemAction action = new PickUpItemAction();
        action.perform(event, outcome, cache);

        // Assert the outcome.
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, SOLDIER_UNIT_TYPE_ID, barracksId));
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, SOLDIER_UNIT_TYPE_ID, null));
    }
}
