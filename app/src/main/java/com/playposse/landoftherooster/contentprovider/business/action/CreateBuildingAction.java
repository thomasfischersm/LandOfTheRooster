package com.playposse.landoftherooster.contentprovider.business.action;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.analytics.Analytics;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.LocationUpdateEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.CreateBuildingPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

/**
 * A {@link BusinessAction} that creates a new building.
 */
public class CreateBuildingAction implements BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        CreateBuildingPreconditionOutcome outcome =
                (CreateBuildingPreconditionOutcome) preconditionOutcome;
        LocationUpdateEvent locationEvent = (LocationUpdateEvent) event;
        BuildingDiscoveryRepository buildingDiscoveryRepository =
                BuildingDiscoveryRepository.get(dataCache.getDao());
        BuildingType buildingType = buildingDiscoveryRepository.getNextBuildingType();

        // Create building.
        Building building =
                createBuilding(dataCache, outcome.getBuildingId(), locationEvent.getLatLng());
        buildingDiscoveryRepository.moveToNextBuildingType();

        // Create MapMarker.
        createMapMarker(dataCache, buildingType, building);

        // Trigger BuildingCreatedEvent.
        BusinessEngine.get().triggerDelayedEvent(new BuildingCreatedEvent(building.getId()));

        // Log to analytics.
        Analytics.reportEvent(Analytics.AppEvent.DISCOVER_BUILDING);
    }

    private Building createBuilding(
            BusinessDataCache dataCache,
            Long buildingTypeId,
            LatLng latLng) {

        Building building = new Building(buildingTypeId, latLng.latitude, latLng.longitude);

        DaoEventRegistry.get(dataCache.getDao()).insert(building);

        return building;
    }

    private void createMapMarker(
            BusinessDataCache dataCache,
            BuildingType buildingType,
            Building building) {

        // Check for battle building.
        boolean isReady = buildingType.isBattleBuilding();

        MapMarker mapMarker = new MapMarker(
                MapMarker.BUILDING_MARKER_TYPE,
                buildingType.getIcon(),
                buildingType.getName(),
                0,
                0,
                isReady,
                building.getId(),
                buildingType.getId());
        DaoEventRegistry.get(dataCache.getDao()).insert(mapMarker);
    }
}
