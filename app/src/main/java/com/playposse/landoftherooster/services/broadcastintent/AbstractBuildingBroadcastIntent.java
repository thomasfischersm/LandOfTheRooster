package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * A broadcast {@link Intent} that passes a buildingId.
 */
public class AbstractBuildingBroadcastIntent implements RoosterBroadcastIntent {

    private static final String BUILDING_ID_EXTRA = "buildingId";
    private static final int DEFAULT_NULL_VALUE = -1;

    private final String EVENT_NAME = getClass().getName();

    private long buildingId;

    /**
     * Default constructor called by reflection.
     */
    AbstractBuildingBroadcastIntent() {
    }

    public AbstractBuildingBroadcastIntent(long buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public void createFromIntent(Intent localIntent) {
        buildingId = localIntent.getLongExtra(BUILDING_ID_EXTRA, DEFAULT_NULL_VALUE);
    }

    @Override
    public Intent createLocalIntent() {
        Intent intent = new Intent(EVENT_NAME);
        intent.putExtra(BUILDING_ID_EXTRA, buildingId);
        return intent;
    }

    public long getBuildingId() {
        return buildingId;
    }
}
