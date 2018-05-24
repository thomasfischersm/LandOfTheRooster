package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessPrecondition} that respawns a building to be ready for battle.
 */
public class RespawnBattleBuildingPrecondition implements BusinessPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();

        // Exit early if the building has already respawned.
        if (buildingWithType.getBuilding().getLastConquest() == null) {
            return new RespawnBattleBuildingPreconditionOutcome(false);
        }

        // Exit early if the building doesn't support battles.
        Integer enemyUnitCount = buildingWithType.getBuildingType().getEnemyUnitCount();
        Integer enemyUnitTypeId = buildingWithType.getBuildingType().getEnemyUnitTypeId();
        if ((enemyUnitCount == null) || (enemyUnitCount == 0) || (enemyUnitTypeId == null)) {
            return new RespawnBattleBuildingPreconditionOutcome(false);
        }

        // Check if the respawn time has elapsed.
        long lastConquest = buildingWithType.getBuilding().getLastConquest().getTime();
        long delayMs =
                (lastConquest + GameConfig.BATTLE_RESPAWN_DURATION - System.currentTimeMillis());
        boolean hasRespawned = (delayMs <= 0);

        if (!hasRespawned) {
            // schedule for later.
            BusinessEngine.get()
                    .scheduleEvent(delayMs, event);
        }

        return new RespawnBattleBuildingPreconditionOutcome(hasRespawned);
    }
}
