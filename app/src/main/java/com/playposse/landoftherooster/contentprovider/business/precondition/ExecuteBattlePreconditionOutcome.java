package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} that indicates if a battle can start.
 */
public class ExecuteBattlePreconditionOutcome extends PreconditionOutcome {

    public ExecuteBattlePreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
