package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when a new building has been created.
 */
public class BuildingCreatedEvent extends BusinessEvent {

    public BuildingCreatedEvent(Long buildingId) {
        super(buildingId);
    }
}
