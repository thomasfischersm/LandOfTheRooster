package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;

/**
 * A {@link BusinessPrecondition} that evaluates if a user can pick up an item from a building.
 */
public class PickUpItemPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = PickUpItemPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        // Check if the building has the item.
        RoosterDao dao = dataCache.getDao();
        PickUpItemEvent castEvent = (PickUpItemEvent) event;
        Item item = castEvent.getItem();
        long buildingId = dataCache.getBuildingId();
        if (item instanceof ResourceItem) {
            long resourceTypeId = ((ResourceItem) item).getResourceTypeId();
            int resourceAmount = RoosterDaoUtil.getResourceAmount(dao, resourceTypeId, buildingId);
            if (resourceAmount <= 0) {
                Log.i(LOG_TAG, "evaluate: Cannot pick up resource of type " + resourceTypeId
                        + " because the building " + buildingId + " doesn't have any.");
                return new PreconditionOutcome(false);
            }
        } else if (item instanceof UnitItem) {
            long unitTypeId = ((UnitItem) item).getUnitTypeId();
            int unitAmount = RoosterDaoUtil.getUnitAmount(dao, unitTypeId, buildingId);
            if (unitAmount <= 0) {
                Log.i(LOG_TAG, "evaluate: Cannot pick up unit of type " + unitTypeId
                        + " because the building " + buildingId + " doesn't have any.");
                return new PreconditionOutcome(false);
            }
        } else {
            throw new IllegalArgumentException("Encountered unexpected item type: "
                    + item.getClass().getName());
        }

        // Check if user has the carry capacity.
        if (item instanceof ResourceItem) {
            int carryCapacity = dao.getCarryingCapacity() + 1;
            int carryAmount = dao.getResourceCountJoiningUser();
            if (carryAmount + 1 > carryCapacity) {
                Log.i(LOG_TAG, "evaluate: Cannot pick up resource because the user doesn't " +
                        "have enough carry capacity.");
                return new PreconditionOutcome(false);
            }
        }

        return new PickUpItemPreconditionOutcome(true);
    }
}
