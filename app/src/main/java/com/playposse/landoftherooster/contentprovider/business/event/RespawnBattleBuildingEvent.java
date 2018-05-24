package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when a building respawns enemy units to be ready for
 * another fight.
 */
public class RespawnBattleBuildingEvent extends BusinessEvent {

    public RespawnBattleBuildingEvent(Long buildingId) {
        super(buildingId);
    }
}
