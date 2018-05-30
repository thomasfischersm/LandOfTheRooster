package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when the production time has completed to produce a free
 * item.
 */
public class CompleteFreeItemProduction extends BusinessEvent {

    public CompleteFreeItemProduction(Long buildingId) {
        super(buildingId);
    }
}
