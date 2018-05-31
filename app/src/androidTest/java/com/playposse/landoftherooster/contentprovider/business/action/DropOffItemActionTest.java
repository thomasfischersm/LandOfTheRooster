package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.DropOffItemPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link DropOffItemAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DropOffItemActionTest extends AbstractBusinessTest {

    @Test
    public void perform_resource() {
        // Create wheat.
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, null);

        // Create mill.
        long millId = createMillAndMarker(dao);

        // Assert original state.
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, millId));

        // Test action.
        DropOffItemEvent event =
                new DropOffItemEvent(millId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID));
        BusinessDataCache cache = new BusinessDataCache(dao, millId);
        DropOffItemPreconditionOutcome outcome =
                new DropOffItemPreconditionOutcome(true);
        DropOffItemAction action = new DropOffItemAction();
        action.perform(event, outcome, cache);

        // Assert result
        assertEquals(
                0,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, null));
        assertEquals(
                1,
                RoosterDaoUtil.getResourceAmount(dao, WHEAT_RESOURCE_TYPE_ID, millId));
    }


    @Test
    public void perform_unit() {
        // Create peasant.
        long peasantId = GameConfig.PEASANT_ID;
        RoosterDaoUtil.creditUnit(dao, peasantId, 1, null);

        // Create mill.
        long barracksId = createBarracksAndMarker(dao);

        // Assert original state.
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, null));
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, barracksId));

        // Test action.
        DropOffItemEvent event =
                new DropOffItemEvent(barracksId, new UnitItem(peasantId));
        BusinessDataCache cache = new BusinessDataCache(dao, barracksId);
        DropOffItemPreconditionOutcome outcome =
                new DropOffItemPreconditionOutcome(true);
        DropOffItemAction action = new DropOffItemAction();
        action.perform(event, outcome, cache);

        // Assert result
        assertEquals(
                0,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, null));
        assertEquals(
                1,
                RoosterDaoUtil.getUnitAmount(dao, peasantId, barracksId));
    }
}
