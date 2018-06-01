package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessEvent} that is fired when the production time has completed to produce a free
 * item.
 */
public class CompleteFreeProductionEvent extends BusinessEvent {

    public CompleteFreeProductionEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(BusinessDataCache dataCache) {
        schedule(dataCache.getBuildingWithType(), dataCache.getPeasantCount());
    }

    public static void schedule(BuildingWithType buildingWithType, int peasantCount) {
        Long buildingId = buildingWithType.getBuilding().getId();
        Long delayMs =
                ProductionCycleUtil.getRemainingProductionTimeMs(peasantCount, buildingWithType);
        CompleteFreeProductionEvent endEvent =  new CompleteFreeProductionEvent(buildingId);
        BusinessEngine.get().scheduleEvent(delayMs, endEvent);
    }
}
