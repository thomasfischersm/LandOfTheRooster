package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

/**
 * A {@link BusinessPrecondition} that admits an injured unit to a healing building.
 */
public class AdmitUnitToHospitalPrecondition implements BusinessPrecondition{

    private static final String LOG_TAG = AdmitUnitToHospitalPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {

        // Check if the building can heal.
        BuildingType buildingType = dataCache.getBuildingType();
        if (!buildingType.isHealsUnits()) {
            Log.d(LOG_TAG, "evaluate: Can't admit unit because the building doesn't heal: "
                    + dataCache.getBuildingId());
            return new PreconditionOutcome(false);
        }

        // Check if unit is injured.
        AdmitUnitToHospitalEvent castEvent = (AdmitUnitToHospitalEvent) event;
        long unitId = castEvent.getUnitId();
        UnitWithType unitWithType = dataCache.getUnitWithTypeJoiningUser(unitId);
        if (unitWithType == null) {
            throw new IllegalArgumentException("Unit doesn't exist: " + unitId);
        }
        if (unitWithType.getUnit().getHealth() >= unitWithType.getType().getHealth()) {
            Log.d(LOG_TAG, "evaluate: Can't admit unit because it is already healthy: "
                    + unitId);
            return new PreconditionOutcome(false);
        }

        return new AdmitUnitToHospitalPreconditionOutcome(true, unitWithType);
    }
}
