package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;

/**
 * A {@link BusinessAction} that starts the healing process at a building.
 */
public class InitiateHealingAction implements BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        // Start the healing process.
        Building building = dataCache.getBuilding();
        building.setHealingStarted(new Date());
        RoosterDao dao = dataCache.getDao();
        DaoEventRegistry.get(dao)
                .update(building);

        // Schedule the CompleteHealingEvent.
        CompleteHealingEvent.schedule(dataCache);
    }
}
