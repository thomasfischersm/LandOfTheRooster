package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when a free item has been fully produced.
 */
public class FreeItemProductionSucceededEvent extends BusinessEvent {

    public FreeItemProductionSucceededEvent(Long buildingId) {
        super(buildingId);
    }
}
