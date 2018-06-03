package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
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

        // Check if the unit has recovered
        PickUpUnitFromHospitalEvent castEvent = (PickUpUnitFromHospitalEvent) event;
        UnitWithType unitWithType = hasUnitRecovered(castEvent, dataCache);
        if (unitWithType == null) {
            Log.i(LOG_TAG, "evaluate: Cannot pick up unit because there are not recovered " +
                    castEvent.getUnitId());
            return new PreconditionOutcome(false);
        }


        return new PickUpUnitFromHospitalPreconditionOutcome(true, unitWithType);
    }

    @Nullable
    private UnitWithType hasUnitRecovered(
            PickUpUnitFromHospitalEvent event,
            BusinessDataCache dataCache) {

        long unitId = event.getUnitId();
        List<UnitWithType> recoveredUnitsWithType = dataCache.getRecoveredUnitsWithType();
        for (UnitWithType unitWithType : recoveredUnitsWithType) {
            Unit unit = unitWithType.getUnit();
            if (unit.getId() == unitId) {
                if (!unitWithType.isInjured()) {
                    return unitWithType;
                } else {
                    Log.i(LOG_TAG, "hasUnitRecovered: The unit has not yet fully recovered: "
                            + unitId);
                    return null;
                }
            }
        }

        Log.i(LOG_TAG, "hasUnitRecovered: Couldn't find unit " + unitId + " at the building "
                + event.getBuildingId());
        return null;
    }
}
