package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

import javax.annotation.Nullable;

/**
 * A {@link BusinessEvent} that's fired after a peasant has been assigned to a building. This lets
 * dialogs know to update the production or healing estimate.
 */
public class PostAssignPeasantEvent extends BusinessEvent {

    public PostAssignPeasantEvent(@Nullable Long buildingId) {
        super(buildingId);
    }
}
