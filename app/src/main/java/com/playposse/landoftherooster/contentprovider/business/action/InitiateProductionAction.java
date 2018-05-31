package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.InitiateProductionPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.Date;

/**
 * An {@link BusinessAction} that starts the production at a building.
 */
public class InitiateProductionAction extends BusinessAction {

    private static final String LOG_TAG = InitiateProductionAction.class.getSimpleName();

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

        InitiateProductionPreconditionOutcome castPreconditionOutCome =
                (InitiateProductionPreconditionOutcome) preconditionOutcome;
        Integer possibleProductionCount = castPreconditionOutCome.getPossibleProductionCount();
        if (possibleProductionCount == null) {
            throw new NullPointerException();
        }

        // Update building to start production.
        building.setProductionStart(new Date());
        DaoEventRegistry.get(dataCache.getDao()).update(building);

        // Schedule production completed event.
        CompleteProductionEvent.schedule(dataCache);
    }
}
