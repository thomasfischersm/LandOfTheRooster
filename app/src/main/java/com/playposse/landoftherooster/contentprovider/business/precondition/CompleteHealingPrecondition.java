package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.action.InitiateHealingAction;
import com.playposse.landoftherooster.contentprovider.business.event.CompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.InitiateHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.List;

/**
 * A {@link BusinessPrecondition} that checks if a unit can complete the healing action.
 */
public class CompleteHealingPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = CompleteHealingPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        // Check if the building is a healing building.
        CompleteHealingEvent castEvent = (CompleteHealingEvent) event;
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Building building = buildingWithType.getBuilding();
        long buildingId = building.getId();
        if (!buildingWithType.getBuildingType().isHealsUnits()) {
            Log.i(LOG_TAG, "evaluate: Cannot heal because the building is not a healing " +
                    "building: " + buildingId);
            return new PreconditionOutcome(false);
        }

        // Find the least sick unit.
        List<UnitWithType> injuredUnitsWithType = dataCache.getInjuredUnitsWithType();
        if (injuredUnitsWithType.size() == 0) {
            Log.i(LOG_TAG, "evaluate: There is no unit to heal: " + buildingId);
            return new PreconditionOutcome(false);
        }
        UnitWithType unitWithType = injuredUnitsWithType.get(0);

        // Ensure that healing has started.
        if (building.getHealingStarted() == null) {
            Log.i(LOG_TAG, "evaluate: Cannot complete healing because healing hasn't started!");
            BusinessEngine.get()
                    .triggerDelayedEvent(new InitiateHealingEvent(buildingId));
            return new PreconditionOutcome(false);
        }

        // Check if the healing time has been used up.
        long healingTimeMs = System.currentTimeMillis() - building.getHealingStarted().getTime();
        int peasantCount = dataCache.getPeasantCount();
        long neededHealingMs = unitWithType.getHealingTimeMs(peasantCount);
        long extraTimeMs = healingTimeMs - neededHealingMs;
        if (extraTimeMs < 0) {
            Log.i(LOG_TAG, "evaluate: The healing time hasn't completed yet for building: "
                    + buildingId);
            InitiateHealingAction.scheduleCompleteHealingEvent(dataCache);
            return new PreconditionOutcome(false);
        }

        // Continue to action
        return new CompleteHealingPreconditionOutcome(true, unitWithType, extraTimeMs);
    }
}
