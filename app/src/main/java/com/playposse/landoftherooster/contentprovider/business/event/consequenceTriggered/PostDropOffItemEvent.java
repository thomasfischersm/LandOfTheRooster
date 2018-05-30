package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;

/**
 * A {@link BusinessEvent} that happens when a user deposits a resource or unit at a building for
 * production.
 */
public class PostDropOffItemEvent extends BusinessEvent {

    private final Item item;

    private PostDropOffItemEvent(long buildingId, Item item) {
        super(buildingId);

        this.item = item;
    }

    public static PostDropOffItemEvent createForResource(long buildingId, long resourceTypeId) {
        return new PostDropOffItemEvent(buildingId, new ResourceItem(resourceTypeId));
    }

    public static PostDropOffItemEvent createForUnit(long buildingId, long unitTypeId) {
        return new PostDropOffItemEvent(buildingId, new UnitItem(unitTypeId));
    }

    public Item getItem() {
        return item;
    }
}
