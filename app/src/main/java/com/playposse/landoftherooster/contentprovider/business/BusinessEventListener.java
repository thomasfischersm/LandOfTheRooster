package com.playposse.landoftherooster.contentprovider.business;

/**
 * An interface that allows app modules to listen to {@link BusinessEvent} being fired. The
 * listeners are always notified last.
 */
public interface BusinessEventListener {

    void onEvent(BusinessEvent event, BusinessDataCache cache);
}
