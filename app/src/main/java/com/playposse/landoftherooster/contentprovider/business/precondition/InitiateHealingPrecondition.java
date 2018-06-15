package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;

/**
 * A {@link BusinessPrecondition} that starts the healing process at a healing building.
 */
public class InitiateHealingPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = InitiateHealingPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        // Check if it is a healing building.
        BuildingType buildingType = dataCache.getBuildingType();
        long buildingId = dataCache.getBuildingId();
        if (!buildingType.isHealsUnits()) {
            Log.d(LOG_TAG, "evaluate: Cannot start the healing process because it is not a " +
                    "healing building: " + buildingId);
            return new PreconditionOutcome(false);
        }

        // Check if the healing process has already started.
        Building building = dataCache.getBuilding();
        if (building.getHealingStarted() != null) {
            Log.d(LOG_TAG, "evaluate: Cannot start healing because healing has already " +
                    "started.");
            return new PreconditionOutcome(false);
        }

        // Check if there is at least one injured unit admitted.
        if (dataCache.getHealingUnitCount() <= 0) {
            Log.d(LOG_TAG, "evaluate: Cannot start the healing process because there is no " +
                    "injured unit at building: " + buildingId);
            return new PreconditionOutcome(false);
        }

        // Start the healing process.
        return new PreconditionOutcome(true);
    }
}
