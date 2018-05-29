package com.playposse.landoftherooster.contentprovider.business.event.mixedTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is triggered when a building has started production.
 */
public class ItemProductionStartedEvent extends BusinessEvent {

    private final int possibleProductionCount;

    public ItemProductionStartedEvent(Long buildingId, int possibleProductionCount) {
        super(buildingId);

        this.possibleProductionCount = possibleProductionCount;
    }

    public int getPossibleProductionCount() {
        return possibleProductionCount;
    }
}
