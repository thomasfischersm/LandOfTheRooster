package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when an injured unit has been admitted.
 */
public class PostAdmitUnitToHospitalEvent extends BusinessEvent {

    private final boolean lastInjuredUnitAdmitted;

    public PostAdmitUnitToHospitalEvent(Long buildingId, boolean lastInjuredUnitAdmitted) {
        super(buildingId);

        this.lastInjuredUnitAdmitted = lastInjuredUnitAdmitted;
    }

    public boolean isLastInjuredUnitAdmitted() {
        return lastInjuredUnitAdmitted;
    }
}
