package com.playposse.landoftherooster.services.broadcastintent;

/**
 * A {@link RoosterBroadcastIntent} that indicates the the user has entered the geo fence of a
 * building. This intent will trigger a dialog that lets the user interact with the building.
 */
public class BuildingAvailableBroadcastIntent extends AbstractBuildingBroadcastIntent {

    public BuildingAvailableBroadcastIntent() {
    }

    public BuildingAvailableBroadcastIntent(long buildingId) {
        super(buildingId);
    }
}
