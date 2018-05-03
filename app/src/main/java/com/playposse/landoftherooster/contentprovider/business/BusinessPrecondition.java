package com.playposse.landoftherooster.contentprovider.business;

/**
 * A precondition that has to evaluate to true before the relevant action can be executed.
 */
public interface BusinessPrecondition {

    /**
     * Evaluates if the preconditions to execute an action are satisfied. The preconditions may
     * indicate that the action is executed multiple times
     */
    PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache);
}
