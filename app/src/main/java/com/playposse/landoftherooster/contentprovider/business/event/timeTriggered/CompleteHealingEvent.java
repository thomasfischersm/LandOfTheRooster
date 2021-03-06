package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.List;

/**
 * A {@link BusinessEvent} that handles the situation of a building completing the healing of a
 * unit.
 */
public class CompleteHealingEvent extends BusinessEvent {

    public CompleteHealingEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(BusinessDataCache dataCache) {
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
        // TODO: If there is extra healing time left unused, pass it on to the next event.
    }

}
