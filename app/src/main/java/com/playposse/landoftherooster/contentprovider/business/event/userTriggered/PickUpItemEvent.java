package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

import javax.annotation.Nullable;

/**
 * A {@link BusinessEvent} that is triggered when a user wants to pick up an item from a building.
 */
public class PickUpItemEvent extends BusinessEvent {

    private final Item item;

    public PickUpItemEvent(@Nullable Long buildingId, Item item) {
        super(buildingId);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
