package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

/**
 * A {@link BusinessAction} that handles a user trying to pick up an item from a building.
 */
public class PickUpItemAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();
        PickUpItemEvent castEvent = (PickUpItemEvent) event;
        Item item = castEvent.getItem();
        long buildingId = dataCache.getBuildingId();

        // Transfer item.
        if (item instanceof ResourceItem) {
            long resourceTypeId = ((ResourceItem) item).getResourceTypeId();
            RoosterDaoUtil.creditResource(dao, resourceTypeId, -1, buildingId);
            RoosterDaoUtil.creditResource(dao, resourceTypeId, 1, null);
        } else if (item instanceof UnitItem) {
            long unitTypeId = ((UnitItem) item).getUnitTypeId();
            RoosterDaoUtil.transferUnitFromBuilding(dao, unitTypeId, buildingId);
        } else {
            throw new IllegalArgumentException("Encountered unexpected item type: "
                    + item.getClass().getName());
        }

        // Trigger post event.
        PostPickUpItemEvent postEvent = new PostPickUpItemEvent(event.getBuildingId(), item);
        BusinessEngine.get()
                .triggerDelayedEvent(postEvent);
    }
}
