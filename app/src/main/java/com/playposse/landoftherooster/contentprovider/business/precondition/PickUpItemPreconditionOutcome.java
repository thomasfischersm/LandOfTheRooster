package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} for trying to pick up an item from a building.
 */
public class PickUpItemPreconditionOutcome extends PreconditionOutcome{

    public PickUpItemPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
