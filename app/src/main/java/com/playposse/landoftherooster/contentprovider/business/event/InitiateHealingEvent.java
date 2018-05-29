package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that fires when a healing building starts healing a unit.
 */
public class InitiateHealingEvent extends BusinessEvent {

    public InitiateHealingEvent(Long buildingId) {
        super(buildingId);
    }
}
