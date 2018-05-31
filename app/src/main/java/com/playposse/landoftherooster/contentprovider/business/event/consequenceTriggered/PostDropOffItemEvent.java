package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that happens when a user deposits a resource or unit at a building for
 * production.
 */
public class PostDropOffItemEvent extends BusinessEvent {

    private final Item item;

    public PostDropOffItemEvent(long buildingId, Item item) {
        super(buildingId);

        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
