package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import javax.annotation.Nullable;

/**
 * A {@link PreconditionOutcome} that indicates if a unit can complete the healing action.
 */
public class CompleteHealingPreconditionOutcome extends PreconditionOutcome {

    @Nullable private final UnitWithType unitWithType;
    @Nullable private final Long extraHealingTime;

    CompleteHealingPreconditionOutcome(
            Boolean isSuccess,
            @Nullable UnitWithType unitWithType,
            @Nullable Long extraHealingTime) {

        super(isSuccess);

        this.unitWithType = unitWithType;
        this.extraHealingTime = extraHealingTime;
    }

    @Nullable
    public UnitWithType getUnitWithType() {
        return unitWithType;
    }

    @Nullable
    public Long getExtraHealingTime() {
        return extraHealingTime;
    }
}
