package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that is fired when a unit is injured.
 */
public class UnitInjuredEvent extends BusinessEvent {

    public UnitInjuredEvent() {
        super(null);
    }
}
