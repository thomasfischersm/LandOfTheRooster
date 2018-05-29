package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;

/**
 * A {@link BusinessEvent} that happens when a user deposits a resource or unit at a building for
 * production.
 */
public class UserDropsOffItemEvent extends BusinessEvent {

    private final Item item;

    private UserDropsOffItemEvent(long buildingId, Item item) {
        super(buildingId);

        this.item = item;
    }

    public static UserDropsOffItemEvent createForResource(long buildingId, long resourceTypeId) {
        return new UserDropsOffItemEvent(buildingId, new ResourceItem(resourceTypeId));
    }

    public static UserDropsOffItemEvent createForUnit(long buildingId, long unitTypeId) {
        return new UserDropsOffItemEvent(buildingId, new UnitItem(unitTypeId));
    }

    public Item getItem() {
        return item;
    }
}
