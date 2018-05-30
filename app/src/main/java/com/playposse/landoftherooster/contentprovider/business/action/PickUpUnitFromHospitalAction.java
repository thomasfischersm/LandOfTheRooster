package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpUnitFromHospitalPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

/**
 * A {@link BusinessAction} that picks up a recovered unit from a healing building.
 */
public class PickUpUnitFromHospitalAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome outcome,
            BusinessDataCache dataCache) {

        // Transfer unit to user.
        RoosterDao dao = dataCache.getDao();
        PickUpUnitFromHospitalPreconditionOutcome castOutcome =
                (PickUpUnitFromHospitalPreconditionOutcome) outcome;
        UnitWithType unitWithType = castOutcome.getUnitWithType();
        Unit unit = unitWithType.getUnit();
        unit.setLocatedAtBuildingId(null);
        DaoEventRegistry.get(dao)
                .update(unit);

        // Fire post action event.
        PostPickUpUnitFromHospitalEvent postEvent =
                new PostPickUpUnitFromHospitalEvent(dataCache.getBuildingId(), unit.getId());
        BusinessEngine.get()
                .triggerDelayedEvent(postEvent);
    }
}
