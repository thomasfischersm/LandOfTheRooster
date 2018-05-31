package com.playposse.landoftherooster.contentprovider.business.event.locationTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * A {@link BusinessEvent} that fires when the user enters the zone of a building.
 */
public class BuildingZoneEnteredEvent extends BusinessEvent {

    private final BuildingWithType buildingWithType;

    public BuildingZoneEnteredEvent(BuildingWithType buildingWithType) {
        super(buildingWithType.getBuilding().getId());

        this.buildingWithType = buildingWithType;
    }

    public BuildingWithType getBuildingWithType() {
        return buildingWithType;
    }
}
