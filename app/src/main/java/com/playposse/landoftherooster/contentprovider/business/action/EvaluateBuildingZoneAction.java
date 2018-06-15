package com.playposse.landoftherooster.contentprovider.business.action;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingZoneRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneEnteredEvent;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneExitedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.LocationUpdateEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessAction} that analyzes a location update to fire events depending on if a user
 * moved into or out of a building zone.
 */
public class EvaluateBuildingZoneAction implements BusinessAction {

    private static final String LOG_TAG = EvaluateBuildingZoneAction.class.getSimpleName();

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();
        BuildingZoneRepository buildingZoneRepository = BuildingZoneRepository.get(dao);
        BuildingWithType lastBuildingWithType = buildingZoneRepository.getCurrentBuildingWithType();

        // Determine new location.
        final LatLng latLng;
        if (event instanceof LocationUpdateEvent) {
            LocationUpdateEvent castEvent = (LocationUpdateEvent) event;
            latLng = castEvent.getLatLng();
        } else if (event instanceof BuildingCreatedEvent) {
            // The location hasn't changed but a new building has appeared at the location.
            latLng = buildingZoneRepository.getCurrentLatLng();
        } else {
            throw new IllegalArgumentException("Encountered event of an unexpected type: "
                    + event.getClass().getName());
        }

        // Update location.
        buildingZoneRepository.updateLocation(latLng);
        BuildingWithType currentBuildingWithType =
                buildingZoneRepository.getCurrentBuildingWithType();

        // Maybe trigger building zone events.
        if ((lastBuildingWithType == null) && (currentBuildingWithType != null)) {
            Log.d(LOG_TAG, "perform: Entered building zone: "
                    + currentBuildingWithType.getBuilding().getId());
            BusinessEngine.get()
                    .triggerDelayedEvent(new BuildingZoneEnteredEvent(currentBuildingWithType));
        } else if ((lastBuildingWithType != null) && (currentBuildingWithType == null)) {
            Log.d(LOG_TAG, "perform: Exited building zone: "
                    + lastBuildingWithType.getBuilding().getId());
            BusinessEngine.get()
                    .triggerDelayedEvent(new BuildingZoneExitedEvent(lastBuildingWithType));
        } else if ((lastBuildingWithType != null)
                && (currentBuildingWithType != null)
                && (lastBuildingWithType.getBuilding().getId()
                != currentBuildingWithType.getBuilding().getId())) {
            // Directly went from one building zone to another.
            BusinessEngine.get()
                    .triggerDelayedEvent(new BuildingZoneEnteredEvent(currentBuildingWithType));
            BusinessEngine.get()
                    .triggerDelayedEvent(new BuildingZoneExitedEvent(lastBuildingWithType));
        }
    }
}
