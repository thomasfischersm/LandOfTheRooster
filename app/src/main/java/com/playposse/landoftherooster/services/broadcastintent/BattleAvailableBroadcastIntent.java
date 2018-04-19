package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * A broadcast {@link Intent} that tells the map activity that the user can engage in a battle.
 */
public class BattleAvailableBroadcastIntent implements RoosterBroadcastIntent {

    private static final String EVENT_NAME = BattleAvailableBroadcastIntent.class.getName();
    private static final String BUILDING_ID_EXTRA = "buildingId";

    private long buildingId;

    /**
     * Default constructor called by reflection.
     */
    BattleAvailableBroadcastIntent() {
    }

    public BattleAvailableBroadcastIntent(long buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public void createFromIntent(Intent localIntent) {
        buildingId = localIntent.getLongExtra(BUILDING_ID_EXTRA, -1);
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
