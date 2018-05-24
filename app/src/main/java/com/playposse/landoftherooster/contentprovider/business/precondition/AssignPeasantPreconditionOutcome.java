package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} that says if a user can assign a peasant to a building.
 */
public class AssignPeasantPreconditionOutcome extends PreconditionOutcome {

    public AssignPeasantPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
