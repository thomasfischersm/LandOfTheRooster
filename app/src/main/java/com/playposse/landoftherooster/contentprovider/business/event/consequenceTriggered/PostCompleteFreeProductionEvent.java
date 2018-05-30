package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;

/**
 * A {@link BusinessEvent} that is fired when a free item has been fully produced.
 */
public class PostCompleteFreeProductionEvent extends BusinessEvent {

    private final Item producedItem;

    public PostCompleteFreeProductionEvent(Long buildingId, Item producedItem) {
        super(buildingId);

        this.producedItem = producedItem;
    }

    public Item getProducedItem() {
        return producedItem;
    }
}
