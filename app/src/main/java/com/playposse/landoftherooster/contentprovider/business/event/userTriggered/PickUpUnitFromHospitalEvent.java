package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * An {@link BusinessEvent} that is triggered by and handles a user picking up a recovered unit
 * from a healing building.
 */
public class PickUpUnitFromHospitalEvent extends BusinessEvent {

    public PickUpUnitFromHospitalEvent(Long buildingId) {
        super(buildingId);
    }
}
