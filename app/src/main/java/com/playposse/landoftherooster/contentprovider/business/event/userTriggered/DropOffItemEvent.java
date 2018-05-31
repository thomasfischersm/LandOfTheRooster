package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that handles a user dropping of an item at a building.
 */
public class DropOffItemEvent extends BusinessEvent {

    private final Item item;

    public DropOffItemEvent(Long buildingId, Item item) {
        super(buildingId);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
