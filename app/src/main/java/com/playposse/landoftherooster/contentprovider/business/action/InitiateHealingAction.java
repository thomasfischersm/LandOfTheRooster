package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.CompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;
import java.util.List;

/**
 * A {@link BusinessAction} that starts the healing process at a building.
 */
public class InitiateHealingAction extends BusinessAction {

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
        scheduleCompleteHealingEvent(dataCache);
    }

    public static void scheduleCompleteHealingEvent(BusinessDataCache dataCache) {
        List<UnitWithType> injuredUnitsWithType = dataCache.getInjuredUnitsWithType();

        // Prevent a timing issue from throwing an exception.
        if (injuredUnitsWithType.size() == 0) {
            return;
        }

        // Calculate healing time.
        UnitWithType unitWithType = injuredUnitsWithType.get(0);
        int peasantCount = dataCache.getPeasantCount();
        long healingTimeMs = unitWithType.getHealingTimeMs(peasantCount);

        // Completed time.
        long healingStartedMs = dataCache.getBuilding().getHealingStarted().getTime();
        long completedTimeMs = System.currentTimeMillis() - healingStartedMs;
        long remainingMs = Math.max(healingTimeMs - completedTimeMs, 0);

        // Schedule event.
        long buildingId = dataCache.getBuildingId();
        BusinessEngine.get()
                .scheduleEvent(remainingMs, new CompleteHealingEvent(buildingId));
    }
}
