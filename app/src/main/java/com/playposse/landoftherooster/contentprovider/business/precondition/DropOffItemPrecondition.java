package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;

import java.util.Map;

/**
 * A {@link BusinessPrecondition} that checks if a user can drop off a specified item at a building.
 */
public class DropOffItemPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = DropOffItemPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        DropOffItemEvent castEvent = (DropOffItemEvent) event;
        Item item = castEvent.getItem();

        if (item instanceof ResourceItem) {
            return evaluate(dataCache, (ResourceItem) item);
        } else if (item instanceof UnitItem) {
            return evaluate(dataCache, (UnitItem) item);
        } else {
            throw new IllegalArgumentException("Encountered unexpected item type: "
                    + item.getClass().getName());
        }
    }

    private PreconditionOutcome evaluate(BusinessDataCache dataCache, ResourceItem resourceItem) {
        // Check if the user carries the item.
        long resourceTypeId = resourceItem.getResourceTypeId();
        Map<Long, Integer> resourceMapJoiningUser = dataCache.getResourceMapJoiningUser();
        Integer resourceCount = resourceMapJoiningUser.get(resourceTypeId);
        if ((resourceCount == null) || (resourceCount <= 0)) {
            Log.i(LOG_TAG, "evaluate: Cannot drop of resource type because the user doesn't " +
                    "have it: " + resourceTypeId);
            return new PreconditionOutcome(false);
        }

        // Check if the building has a production rule for the item.
        if (!dataCache.usesResourceTypeAsInput(resourceTypeId)) {
            Log.i(LOG_TAG, "evaluate: The building doesn't use the resource type as an " +
                    "input: " + resourceTypeId);
            return new PreconditionOutcome(false);
        }

        return new DropOffItemPreconditionOutcome(true);
    }

    private PreconditionOutcome evaluate(BusinessDataCache dataCache, UnitItem unitItem) {
        // Check if the user carries the item.
        long unitTypeId = unitItem.getUnitTypeId();
        Map<Long, Integer> unitMapJoiningUser = dataCache.getUnitMapJoiningUser();
        Integer unitCount = unitMapJoiningUser.get(unitTypeId);
        if ((unitCount == null) || (unitCount <= 0)) {
            Log.i(LOG_TAG, "evaluate: Cannot drop of unit type because the user doesn't have " +
                    "it: " + unitTypeId);
            return new PreconditionOutcome(false);
        }

        // Check if the building has a production rule for the item.
        if (!dataCache.usesUnitTypeAsInput(unitTypeId)) {
            Log.i(LOG_TAG, "evaluate: The building doesn't use the unit type as an " +
                    "input: " + unitTypeId);
            return new PreconditionOutcome(false);
        }

        return new DropOffItemPreconditionOutcome(true);
    }
}
