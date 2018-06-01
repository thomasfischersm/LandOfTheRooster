package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.mixedTriggered.InitiateHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.AdmitUnitToHospitalPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.List;

/**
 * A {@link BusinessAction} that handles admitting an injured unit to a building for the user.
 */
public class AdmitUnitToHospitalAction implements BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        AdmitUnitToHospitalPreconditionOutcome outcome =
                (AdmitUnitToHospitalPreconditionOutcome) preconditionOutcome;
        UnitWithType unitWithType = outcome.getUnitWithType();
        Unit unit = unitWithType.getUnit();
        long buildingId = dataCache.getBuildingId();

        // Transfer unit.
        unit.setLocatedAtBuildingId(buildingId);
        RoosterDao dao = dataCache.getDao();
        DaoEventRegistry.get(dao)
                .update(unit);

        // Fire event to potentially initiate healing.
        BusinessEngine.get()
                .triggerDelayedEvent(new InitiateHealingEvent(buildingId));

        // Check if this was the last injured unit joining the user.
        List<UnitWithType> injuredUnitsWithType = dataCache.getInjuredUnitsWithTypeJoiningUser();
        boolean hasInjuredUnit = (injuredUnitsWithType.size() > 1)
                || ((injuredUnitsWithType.size() == 1)
                && (injuredUnitsWithType.get(0).getUnit().getId() != unit.getId()));

        PostAdmitUnitToHospitalEvent postEvent =
                new PostAdmitUnitToHospitalEvent(buildingId, !hasInjuredUnit);
        BusinessEngine.get()
                .triggerDelayedEvent(postEvent);
    }
}
