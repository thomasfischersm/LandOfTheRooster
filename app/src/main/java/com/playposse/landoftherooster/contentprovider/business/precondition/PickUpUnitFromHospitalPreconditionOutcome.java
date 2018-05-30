package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

/**
 * A {@link PreconditionOutcome} from checking if a recovered unit is available at a healing
 * building.
 */
public class PickUpUnitFromHospitalPreconditionOutcome extends PreconditionOutcome {

    private final UnitWithType unitWithType;

    public PickUpUnitFromHospitalPreconditionOutcome(Boolean isSuccess, UnitWithType unitWithType) {
        super(isSuccess);

        this.unitWithType = unitWithType;
    }

    public UnitWithType getUnitWithType() {
        return unitWithType;
    }
}
