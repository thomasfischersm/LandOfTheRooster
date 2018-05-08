package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when the production time has completed to produce a free
 * item.
 */
public class FreeItemProductionEndedEvent extends BusinessEvent {

    public FreeItemProductionEndedEvent(Long buildingId) {
        super(buildingId);
    }
}
