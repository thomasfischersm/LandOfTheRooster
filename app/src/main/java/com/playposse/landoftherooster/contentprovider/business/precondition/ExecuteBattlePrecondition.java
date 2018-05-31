package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessPrecondition} that checks if a battle can start. The building may need to
 * respawn before a battle can start.
 */
public class ExecuteBattlePrecondition implements BusinessPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();

        // Exit early if the building doesn't support battles.
        Integer enemyUnitCount = buildingWithType.getBuildingType().getEnemyUnitCount();
        Integer enemyUnitTypeId = buildingWithType.getBuildingType().getEnemyUnitTypeId();
        if ((enemyUnitCount == null) || (enemyUnitCount == 0) || (enemyUnitTypeId == null)) {
            return new PreconditionOutcome(false);
        }

        // Exit early if the building has already respawned.
        if (buildingWithType.getBuilding().getLastConquest() == null) {
            return new PreconditionOutcome(true);
        }

        // Check if the building has respawned.
        long lastConquest = buildingWithType.getBuilding().getLastConquest().getTime();
        boolean hasRespawned =
                (lastConquest + GameConfig.BATTLE_RESPAWN_DURATION < System.currentTimeMillis());

        return new PreconditionOutcome(hasRespawned);
    }
}
