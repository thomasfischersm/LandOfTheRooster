package com.playposse.landoftherooster.contentprovider.business.event.locationTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessEvent} that is triggered when the user exits the zone of a building.
 */
public class BuildingZoneExitedEvent extends BusinessEvent {

    private final BuildingWithType buildingWithType;

    public BuildingZoneExitedEvent(BuildingWithType buildingWithType) {
        super(buildingWithType.getBuilding().getId());

        this.buildingWithType = buildingWithType;
    }

    public BuildingWithType getBuildingWithType() {
        return buildingWithType;
    }
}
