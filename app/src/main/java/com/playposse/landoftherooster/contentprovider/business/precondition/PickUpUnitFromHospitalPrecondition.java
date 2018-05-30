package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.List;

/**
 * A {@link BusinessPrecondition} that checks if a user can pick up a recovered unit from a healing
 * building.
 */
public class PickUpUnitFromHospitalPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = PickUpUnitFromHospitalPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        // Check if it is a healing building.
        BuildingType buildingType = dataCache.getBuildingType();
        if (!buildingType.isHealsUnits()) {
            Log.i(LOG_TAG, "evaluate: Cannot pick up unit because the building does not heal: "
                    + dataCache.getBuildingId());
            return new PreconditionOutcome(false);
        }

        // Check if a recovered unit is available for pickup.
        List<UnitWithType> recoveredUnitsWithType = dataCache.getRecoveredUnitsWithType();
        if (recoveredUnitsWithType.size() <= 0) {
            Log.i(LOG_TAG, "evaluate: Cannot pick up unit because there are no recovered " +
                    "units at the building: " + dataCache.getBuildingId());
            return new PreconditionOutcome(false);
        }

        return new PickUpUnitFromHospitalPreconditionOutcome(
                true,
                recoveredUnitsWithType.get(0));
    }
}
