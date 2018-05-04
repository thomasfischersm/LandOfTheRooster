package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionEndedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionStartedEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.StartItemProductionPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;

/**
 * An {@link BusinessAction} that starts the production at a building.
 */
public class StartItemProductionAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        // Get useful references.
        Building building = dataCache.getBuilding();
        if (building == null) {
            throw new IllegalStateException("Expected a building.");
        }

        StartItemProductionPreconditionOutcome castPreconditionOutCome =
                (StartItemProductionPreconditionOutcome) preconditionOutcome;
        Integer possibleProductionCount = castPreconditionOutCome.getPossibleProductionCount();
        if (possibleProductionCount == null) {
            throw new NullPointerException();
        }

        // Update building to start production.
        building.setProductionStart(new Date());
        DaoEventRegistry.get(dataCache.getDao()).update(building);

        // Fire production started event.
        BusinessEngine.get().triggerEvent(
                new ItemProductionStartedEvent(event.getBuildingId(), possibleProductionCount));

        // Schedule production completed event.
        BusinessEngine.get().scheduleEvent(
                ProductionCycleUtil.getRemainingProductionTimeMs(dataCache.getUnitMap(), dataCache.getBuildingWithType()),
                new ItemProductionEndedEvent(event.getBuildingId()));
    }
}
