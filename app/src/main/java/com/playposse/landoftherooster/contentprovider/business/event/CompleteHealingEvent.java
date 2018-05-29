package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that handles the situation of a building completing the healing of a
 * unit.
 */
public class CompleteHealingEvent extends BusinessEvent {

    public CompleteHealingEvent(Long buildingId) {
        super(buildingId);
    }
}
