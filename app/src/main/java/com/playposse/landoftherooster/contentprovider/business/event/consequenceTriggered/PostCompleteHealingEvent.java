package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired after a unit has been healed for any logic that wants
 * to attach itself after that.
 */
public class PostCompleteHealingEvent extends BusinessEvent {

    public PostCompleteHealingEvent(Long buildingId) {
        super(buildingId);
    }
}
