package com.playposse.landoftherooster.contentprovider.business.event;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that fires when the building production rule has finished producing an
 * item.
 */
public class ItemProductionSucceededEvent extends BusinessEvent {

    private final Item producedItem;

    public ItemProductionSucceededEvent(Long buildingId, Item producedItem) {
        super(buildingId);

        this.producedItem = producedItem;
    }

    public Item getProducedItem() {
        return producedItem;
    }
}
