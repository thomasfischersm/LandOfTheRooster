package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;

/**
 * A {@link BusinessAction} that starts the production cycle for a free item.
 */
public class InitiateFreeProductionAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        Building building = dataCache.getBuilding();
        if (building == null) {
            throw new NullPointerException("The building should NOT have been null: "
                    + dataCache.getBuildingId());
        }

        // Start building clock.
        building.setProductionStart(new Date());
        DaoEventRegistry.get(dataCache.getDao()).update(building);

        // Schedule the event for the building completion.
        CompleteFreeProductionEvent.schedule(dataCache);
    }
}
