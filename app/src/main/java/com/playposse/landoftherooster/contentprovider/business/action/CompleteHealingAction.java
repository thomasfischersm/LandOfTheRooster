package com.playposse.landoftherooster.contentprovider.business.action;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.mixedTriggered.InitiateHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.List;

/**
 * A {@link BusinessAction} to complete the healing process at a building.
 */
public class CompleteHealingAction implements BusinessAction {

    private static final String LOG_TAG = CompleteHealingAction.class.getSimpleName();

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {


        // Heal the unit.
        RoosterDao dao = dataCache.getDao();
        List<UnitWithType> injuredUnitsWithType = dataCache.getInjuredUnitsWithType();
        if (injuredUnitsWithType.size() > 0) {
            UnitWithType unitWithType = injuredUnitsWithType.get(0);
            UnitType unitType = unitWithType.getType();
            Unit unit = unitWithType.getUnit();
            unit.setHealth(unitType.getHealth());
            DaoEventRegistry.get(dao)
                    .update(unit);
            Log.i(LOG_TAG, "perform: Healed unit: " + unit.getId());
        }

        // Clear healingStarted if the last unit has been healed.
        Building building = dataCache.getBuilding();
        building.setHealingStarted(null);
        DaoEventRegistry.get(dao)
                .update(building);


        // Initiate the next healing event.
        long buildingId = dataCache.getBuildingId();
        injuredUnitsWithType = dataCache.getInjuredUnitsWithType();
        boolean lastUnitHealed = (injuredUnitsWithType.size() == 0);
        if (!lastUnitHealed) {
            BusinessEngine.get()
                    .triggerDelayedEvent(new InitiateHealingEvent(buildingId));
        }

        // Schedule post event for the marker to be updated.
        BusinessEngine.get()
                .triggerDelayedEvent(new PostCompleteHealingEvent(buildingId));
    }
}
