package com.playposse.landoftherooster.contentprovider.business;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * Interface for classes that are called to scheduleWithDefaultDelay timed {@link BusinessEvent}s on startup.
 *
 * <p>For example, a building may start production. The player may exit the app. When the player
 * starts playing again, all these events need to be scheduled in the new instance of the
 * {@link BusinessEngine}.
 */
public interface BusinessStartupInitializer {

    void scheduleIfNecessary(RoosterDao dao, BuildingWithType buildingWithType);
}
