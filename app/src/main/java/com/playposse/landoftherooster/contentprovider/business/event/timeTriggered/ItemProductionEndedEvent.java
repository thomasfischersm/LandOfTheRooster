package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that occurs when the timer for the production of a building has run out.
 */
public class ItemProductionEndedEvent extends BusinessEvent {

    public ItemProductionEndedEvent(Long buildingId) {
        super(buildingId);
    }
}
