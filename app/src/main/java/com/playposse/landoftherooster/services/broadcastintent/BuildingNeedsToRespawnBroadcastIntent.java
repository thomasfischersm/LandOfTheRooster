package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * Created by thoma on 4/6/2018.
 */
public class BuildingNeedsToRespawnBroadcastIntent implements RoosterBroadcastIntent {

    private static final String EVENT_NAME = BuildingNeedsToRespawnBroadcastIntent.class.getName();
    private static final String REMAINING_MS_EXTRA = "remainingMs";

    private long remainingMs;

    /**
     * Default constructor called by reflection.
     */
    public BuildingNeedsToRespawnBroadcastIntent() {
    }

    public BuildingNeedsToRespawnBroadcastIntent(long remainingMs) {
        this.remainingMs = remainingMs;
    }

    @Override
    public void createFromIntent(Intent localIntent) {
        remainingMs = localIntent.getLongExtra(REMAINING_MS_EXTRA, -1);
    }

    @Override
    public Intent createLocalIntent() {
        Intent intent = new Intent(EVENT_NAME);
        intent.putExtra(REMAINING_MS_EXTRA, remainingMs);
        return intent;
    }

    public long getRemainingMs() {
        return remainingMs;
    }
}
