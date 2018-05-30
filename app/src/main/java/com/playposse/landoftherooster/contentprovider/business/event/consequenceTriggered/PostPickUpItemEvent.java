package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that is triggered when the user picks up an item.
 */
public class PostPickUpItemEvent extends BusinessEvent {

    private final Item item;

    public PostPickUpItemEvent(Long buildingId, Item item) {
        super(buildingId);

        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
