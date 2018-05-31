package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.List;

/**
 * A {@link BusinessAction} that handles dropping off an item at a building for the user.
 */
public class DropOffItemAction extends BusinessAction{

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();
        DropOffItemEvent castEvent = (DropOffItemEvent) event;
        Item item = castEvent.getItem();

        // Transfer item.
        long buildingId = dataCache.getBuildingId();
        if (item instanceof ResourceItem) {
            transferResource(dao, (ResourceItem) item, buildingId);
        } else if (item instanceof UnitItem) {
            transferUnit(dataCache, dao, (UnitItem) item, buildingId);
        } else {
            throw new IllegalArgumentException("Encountered unexpected item type: "
                    + item.getClass().getName());
        }

        // Fire post event
        BusinessEngine.get()
                .triggerDelayedEvent(new PostDropOffItemEvent(buildingId, item));
    }

    private void transferResource(RoosterDao dao, ResourceItem item, long buildingId) {
        ResourceItem resourceItem = item;
        long resourceTypeId = resourceItem.getResourceTypeId();
        RoosterDaoUtil.creditResource(dao, resourceTypeId, -1, null);
        RoosterDaoUtil.creditResource(dao, resourceTypeId, 1, buildingId);
    }

    private void transferUnit(
            BusinessDataCache dataCache,
            RoosterDao dao,
            UnitItem item,
            long buildingId) {

        UnitItem unitItem = item;
        long unitTypeId = unitItem.getUnitTypeId();

        List<UnitWithType> unitsWithTypeJoiningUser = dataCache.getUnitsWithTypeJoiningUser();
        UnitWithType unitWithType = RoosterDaoUtil.transferFirstUnitOfType(
                dao,
                unitsWithTypeJoiningUser,
                unitTypeId,
                buildingId);

        if (unitWithType == null) {
            throw new IllegalStateException("Failed to transfer unit of type " + unitTypeId +
                    " to building " + buildingId);
        }
    }
}
