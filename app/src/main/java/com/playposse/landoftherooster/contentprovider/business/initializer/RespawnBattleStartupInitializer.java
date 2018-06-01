package com.playposse.landoftherooster.contentprovider.business.initializer;

import com.playposse.landoftherooster.contentprovider.business.BusinessStartupInitializer;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.RespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Date;

/**
 * A {@link BusinessStartupInitializer} that restarts the events to respawn a building after a
 * battle.
 */
public class RespawnBattleStartupInitializer implements BusinessStartupInitializer {

    @Override
    public void scheduleIfNecessary(RoosterDao dao, BuildingWithType buildingWithType) {
        Date lastConquest = buildingWithType.getBuilding().getLastConquest();
        if (lastConquest != null) {
            RespawnBattleBuildingEvent.schedule(buildingWithType);
        }
    }
}
