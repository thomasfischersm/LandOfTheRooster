package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} that indicates if the healing process at a healing building can
 * start.
 */
public class InitiateHealingPreconditionOutcome extends PreconditionOutcome {

    InitiateHealingPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
