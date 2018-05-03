package com.playposse.landoftherooster.contentprovider.business;

/**
 * An action that updates the database in some way because something should occur.
 */
public abstract class BusinessAction {

    public abstract void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache);
}
