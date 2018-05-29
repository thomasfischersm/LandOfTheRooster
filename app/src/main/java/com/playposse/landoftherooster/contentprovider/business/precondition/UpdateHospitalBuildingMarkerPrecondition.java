package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

/**
 * A {@link BusinessPrecondition} to update a hospital building marker.
 */
public class UpdateHospitalBuildingMarkerPrecondition implements BusinessPrecondition{

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        return new UpdateHospitalBuildingMarkerPreconditionOutcome(true);
    }
}
