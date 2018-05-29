package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import javax.annotation.Nullable;

/**
 * The {@link PreconditionOutcome} when a user admits an injured unit to a hospital.
 */
public class AdmitUnitToHospitalPreconditionOutcome extends PreconditionOutcome{

    @Nullable
    private final UnitWithType unitWithType;

    public AdmitUnitToHospitalPreconditionOutcome(
            Boolean isSuccess,
            @Nullable UnitWithType unitWithType) {
        super(isSuccess);

        this.unitWithType = unitWithType;
    }

    public UnitWithType getUnitWithType() {
        return unitWithType;
    }
}
