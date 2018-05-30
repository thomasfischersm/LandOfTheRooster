package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that fires when the building production rule has finished producing an
 * item.
 */
public class PostCompleteProductionEvent extends BusinessEvent {

    private final Item producedItem;

    public PostCompleteProductionEvent(Long buildingId, Item producedItem) {
        super(buildingId);

        this.producedItem = producedItem;
    }

    public Item getProducedItem() {
        return producedItem;
    }
}
