package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;

/**
 * A {@link BusinessEvent} that occurs when the timer for the production of a building has run out.
 */
public class CompleteProductionEvent extends BusinessEvent {

    private static final String LOG_TAG = CompleteProductionEvent.class.getSimpleName();

    public CompleteProductionEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(BusinessDataCache dataCache) {
        Long remainingMs = ProductionCycleUtil.getRemainingProductionTimeMs(
                dataCache.getUnitMap(),
                dataCache.getBuildingWithType());
        BusinessEngine.get().scheduleEvent(
                remainingMs,
                new CompleteProductionEvent(dataCache.getBuildingId()));
        Log.i(LOG_TAG, "schedule: Scheduled production to finish in " + remainingMs + "ms.");
    }
}
