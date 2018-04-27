package com.playposse.landoftherooster.services.broadcastintent;

/**
 * A {@link RoosterBroadcastIntent} that indicates the the user has entered the geo fence of a
 * hospital building. This intent will trigger a dialog that lets the user interact with the
 * building.
 */
public class HospitalAvailableBroadcastIntent extends AbstractBuildingBroadcastIntent {

    public HospitalAvailableBroadcastIntent() {
    }

    public HospitalAvailableBroadcastIntent(long buildingId) {
        super(buildingId);
    }
}
