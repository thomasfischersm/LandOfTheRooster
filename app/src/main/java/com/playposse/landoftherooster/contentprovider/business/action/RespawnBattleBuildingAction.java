package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostRespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

/**
 * A {@link BusinessAction} that makes a building ready for another battle again.
 */
public class RespawnBattleBuildingAction implements BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        // Respawn the building.
        Building building = dataCache.getBuilding();
        building.setLastConquest(null);
        RoosterDao dao = dataCache.getDao();
        DaoEventRegistry.get(dao)
                .update(building);

        // Trigger post event
        PostRespawnBattleBuildingEvent postEvent =
                new PostRespawnBattleBuildingEvent(dataCache.getBuildingId());
        BusinessEngine.get()
                .triggerDelayedEvent(postEvent);
    }
}
