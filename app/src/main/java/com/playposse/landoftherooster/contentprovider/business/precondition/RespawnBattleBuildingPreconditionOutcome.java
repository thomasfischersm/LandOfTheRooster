package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link PreconditionOutcome} of a check to see if a building is ready to be attacked again.
 */
public class RespawnBattleBuildingPreconditionOutcome extends PreconditionOutcome {

    public RespawnBattleBuildingPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
