package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.business.event.other.LocationUpdateEvent;

/**
 * A {@link BusinessPrecondition} that checks if the user indeed walked the right distance to
 * discover a new building.
 */
public class CreateBuildingPrecondition implements BusinessPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        LocationUpdateEvent locationEvent = (LocationUpdateEvent) event;
        LatLng latLng = locationEvent.getLatLng();
        BuildingDiscoveryRepository buildingDiscoveryRepository =
                BuildingDiscoveryRepository.get(dataCache.getDao());

        // Early exit if all buildings have been discovered.
        if (!buildingDiscoveryRepository.isHasMoreBuildingTypesToDiscover()) {
            return new CreateBuildingPreconditionOutcome(false, null);
        }

        // Check if the right distance for a new building has been reached.
        Float minDistanceFromCurrentBuildings =
                buildingDiscoveryRepository.getMinDistanceFromCurrentBuildings(latLng);
        Float currentDistance = minDistanceFromCurrentBuildings;
        Integer nextDistance = buildingDiscoveryRepository.getNextDistance();
        if ((currentDistance == null)
                || ((currentDistance >= nextDistance)
                && (currentDistance <= nextDistance + GameConfig.MAX_GRACE_DISTANCE))) {
            // Discovered new building.
            long buildingTypeId = buildingDiscoveryRepository.getNextBuildingType().getId();
            return new CreateBuildingPreconditionOutcome(true, buildingTypeId);
        } else {
            // Out of range for new building.
            return new CreateBuildingPreconditionOutcome(false, null);
        }
    }
}
