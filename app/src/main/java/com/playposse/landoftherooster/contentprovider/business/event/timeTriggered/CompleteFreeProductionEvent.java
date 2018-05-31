package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Map;

/**
 * A {@link BusinessEvent} that is fired when the production time has completed to produce a free
 * item.
 */
public class CompleteFreeProductionEvent extends BusinessEvent {

    public CompleteFreeProductionEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(BusinessDataCache dataCache) {
        Long buildingId = dataCache.getBuildingId();
        Map<Long, Integer> unitMap = dataCache.getUnitMap();
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Long delayMs = ProductionCycleUtil.getRemainingProductionTimeMs(unitMap, buildingWithType);
        CompleteFreeProductionEvent endEvent =  new CompleteFreeProductionEvent(buildingId);
        BusinessEngine.get().scheduleEvent(delayMs, endEvent);
    }
}
