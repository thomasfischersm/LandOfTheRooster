package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * A {@link RoosterBroadcastIntent} that indicates the the user has entered the geo fence of a
 * building. This intent will trigger a dialog that lets the user interact with the building.
 */
public class BuildingAvailableBroadcastIntent implements RoosterBroadcastIntent {

    private static final String EVENT_NAME = BuildingAvailableBroadcastIntent.class.getName();
    private static final String BUILDING_ID_EXTRA = "buildingId";

    private int buildingId;

    /**
     * Default constructor for reflection.
     */
    public BuildingAvailableBroadcastIntent() {
    }

    public BuildingAvailableBroadcastIntent(int buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public void createFromIntent(Intent localIntent) {
        buildingId = localIntent.getIntExtra(BUILDING_ID_EXTRA, -1);
    }

    @Override
    public Intent createLocalIntent() {
        Intent intent = new Intent(EVENT_NAME);
        intent.putExtra(BUILDING_ID_EXTRA, buildingId);
        return intent;
    }

    public int getBuildingId() {
        return buildingId;
    }
}
