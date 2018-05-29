package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

/**
 * A {@link PreconditionOutcome} for updating a hospital {@link MapMarker}.
 */
public class UpdateHospitalBuildingMarkerPreconditionOutcome extends PreconditionOutcome {

    public UpdateHospitalBuildingMarkerPreconditionOutcome(Boolean isSuccess) {
        super(isSuccess);
    }
}
