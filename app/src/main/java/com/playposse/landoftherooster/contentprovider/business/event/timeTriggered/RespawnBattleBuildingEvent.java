package com.playposse.landoftherooster.contentprovider.business.event.timeTriggered;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Date;

/**
 * A {@link BusinessEvent} that is fired when a building respawns enemy units to be ready for
 * another fight.
 */
public class RespawnBattleBuildingEvent extends BusinessEvent {

    public RespawnBattleBuildingEvent(Long buildingId) {
        super(buildingId);
    }

    public static void scheduleWithDefaultDelay(long buildingId) {
        int delayMs = GameConfig.BATTLE_RESPAWN_DURATION;
        schedule(buildingId, delayMs);
    }

    public static void schedule(BuildingWithType buildingWithType) {
        long buildingId = buildingWithType.getBuilding().getId();
        Date lastConquest = buildingWithType.getBuilding().getLastConquest();

        if (lastConquest == null) {
            throw new IllegalArgumentException(
                    "LastConquest was null for the building: " + buildingId);
        }

        long alreadyPassedTimeMs = System.currentTimeMillis() - lastConquest.getTime();
        long respawnTime = GameConfig.BATTLE_RESPAWN_DURATION;
        long remainingMs = Math.max(respawnTime - alreadyPassedTimeMs, 0);

        schedule(buildingId, remainingMs);
    }

    private static void schedule(long buildingId, long delayMs) {
        RespawnBattleBuildingEvent respawnEvent = new RespawnBattleBuildingEvent(buildingId);
        BusinessEngine.get()
                .scheduleEvent(delayMs, respawnEvent);
    }
}
