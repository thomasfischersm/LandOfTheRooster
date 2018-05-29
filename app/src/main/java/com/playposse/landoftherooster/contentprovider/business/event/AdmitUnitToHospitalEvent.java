package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that handles the user admitting an injured unit to a hospital type
 * building.
 */
public class AdmitUnitToHospitalEvent extends BusinessEvent {

    private final long unitId;

    public AdmitUnitToHospitalEvent(Long buildingId, long unitId) {
        super(buildingId);

        this.unitId = unitId;
    }

    public long getUnitId() {
        return unitId;
    }
}
