package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that occurs when the timer for the production of a building has run out.
 */
public class ItemProductionCompleteEvent extends BusinessEvent {

    public ItemProductionCompleteEvent(Long buildingId) {
        super(buildingId);
    }
}
