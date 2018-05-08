package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.FreeItemProductionEndedEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;
import java.util.Map;

/**
 * A {@link BusinessAction} that starts the production cycle for a free item.
 */
public class StartFreeItemProductionAction extends BusinessAction {

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
        Long buildingId = event.getBuildingId();
        Map<Long, Integer> unitMap = dataCache.getUnitMap();
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Long delayMs = ProductionCycleUtil.getRemainingProductionTimeMs(unitMap, buildingWithType);
        FreeItemProductionEndedEvent endEvent =  new FreeItemProductionEndedEvent(buildingId);
        BusinessEngine.get().scheduleEvent(delayMs, endEvent);
    }
}
