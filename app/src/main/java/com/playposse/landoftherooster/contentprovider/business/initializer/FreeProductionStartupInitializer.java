package com.playposse.landoftherooster.contentprovider.business.initializer;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessStartupInitializer;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Date;

/**
 * A {@link BusinessStartupInitializer} that restarts free production.
 */
public class FreeProductionStartupInitializer implements BusinessStartupInitializer {

    @Override
    public void scheduleIfNecessary(RoosterDao dao, BuildingWithType buildingWithType) {
        Date productionStart = buildingWithType.getBuilding().getProductionStart();
        if (productionStart != null) {
            BusinessDataCache cache = new BusinessDataCache(dao, buildingWithType);
            int peasantCount = cache.getPeasantCount();
            CompleteFreeProductionEvent.schedule(buildingWithType, peasantCount);
        }
    }
}
