package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;

import javax.annotation.Nullable;

/**
 * A {@link BusinessEvent} that is triggered after a user successfully picks up a unit from a
 * building.
 */
public class PostPickUpUnitFromHospitalEvent extends BusinessEvent {

    @Nullable private final Long unitId;

    public PostPickUpUnitFromHospitalEvent(@Nullable Long buildingId, @Nullable Long unitId) {
        super(buildingId);
        this.unitId = unitId;
    }

    @Nullable
    public Long getUnitId() {
        return unitId;
    }

    public UnitItem getPickedUpUnitItem() {
        return new UnitItem(unitId);
    }
}
