package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when a building respawns enemy units to be ready for
 * another fight.
 */
public class RespawnBattleBuildingEvent extends BusinessEvent {

    public RespawnBattleBuildingEvent(Long buildingId) {
        super(buildingId);
    }

    public static void schedule(long buildingId) {
        int delayMs = GameConfig.BATTLE_RESPAWN_DURATION;
        RespawnBattleBuildingEvent respawnEvent = new RespawnBattleBuildingEvent(buildingId);
        BusinessEngine.get()
                .scheduleEvent(delayMs, respawnEvent);
    }
}
