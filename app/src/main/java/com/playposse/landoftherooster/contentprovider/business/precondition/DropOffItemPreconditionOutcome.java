package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} that indicates if a user can drop off an item at a building.
 */
public class DropOffItemPreconditionOutcome extends PreconditionOutcome {

    public DropOffItemPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
