package com.playposse.landoftherooster.contentprovider.business.initializer;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessStartupInitializer;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Date;

/**
 * A {@link BusinessStartupInitializer} that restarts healing events after the app restarts.
 */
public class HealingStartupInitializer implements BusinessStartupInitializer {

    @Override
    public void scheduleIfNecessary(RoosterDao dao, BuildingWithType buildingWithType) {
        Date healingStarted = buildingWithType.getBuilding().getHealingStarted();
        if (healingStarted != null) {
            BusinessDataCache cache = new BusinessDataCache(dao, buildingWithType);
            CompleteHealingEvent.schedule(cache);
        }
    }
}
