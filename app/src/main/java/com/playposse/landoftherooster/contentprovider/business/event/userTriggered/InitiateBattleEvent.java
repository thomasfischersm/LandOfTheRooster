package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * CA {@link BusinessEvent} that is fired when a user initiates a battle in a building.
 */
public class InitiateBattleEvent extends BusinessEvent {

    public InitiateBattleEvent(Long buildingId) {
        super(buildingId);
    }
}
