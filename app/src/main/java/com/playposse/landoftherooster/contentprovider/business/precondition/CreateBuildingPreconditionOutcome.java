package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

import javax.annotation.Nullable;

/**
 * A {@link PreconditionOutcome} that determines if the building can be constructed.
 */
public class CreateBuildingPreconditionOutcome extends PreconditionOutcome {

    @Nullable
    private final Long buildingTypeId;

    public CreateBuildingPreconditionOutcome(Boolean isSuccess, @Nullable Long buildingTypeId) {
        super(isSuccess);

        this.buildingTypeId = buildingTypeId;
    }

    @Nullable
    public Long getBuildingId() {
        return buildingTypeId;
    }
}
