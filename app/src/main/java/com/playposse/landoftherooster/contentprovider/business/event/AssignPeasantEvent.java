package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * An {@link BusinessEvent} that is triggered when a user assigns a peasant to a building.
 */
public class AssignPeasantEvent extends BusinessEvent {

    public AssignPeasantEvent(Long buildingId) {
        super(buildingId);
    }
}
