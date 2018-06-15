package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessEvent} that occurs when the timer for the production of a building has run out.
 */
public class CompleteProductionEvent extends BusinessEvent {

    private static final String LOG_TAG = CompleteProductionEvent.class.getSimpleName();

    public CompleteProductionEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(BusinessDataCache dataCache) {
        schedule(dataCache.getBuildingWithType(), dataCache.getPeasantCount());
    }

    public static void schedule(BuildingWithType buildingWithType, int buildingPeasantCount) {
        Long remainingMs = ProductionCycleUtil.getRemainingProductionTimeMs(
                buildingPeasantCount,
                buildingWithType);
        long buildingId = buildingWithType.getBuilding().getId();
        BusinessEngine.get().scheduleEvent(
                remainingMs,
                new CompleteProductionEvent(buildingId));
        Log.d(LOG_TAG, "scheduleWithDefaultDelay: Scheduled production to finish in " + remainingMs + "ms.");
    }
}
