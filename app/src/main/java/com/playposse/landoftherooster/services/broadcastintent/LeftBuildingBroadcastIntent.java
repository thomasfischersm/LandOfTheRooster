package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * Created by thoma on 4/1/2018.
 */
public class LeftBuildingBroadcastIntent implements RoosterBroadcastIntent {

    private static final String EVENT_NAME = LeftBuildingBroadcastIntent.class.getName();

    @Override
    public void createFromIntent(Intent localIntent) {
        // Nothing to do because there are no parameters.
    }

    @Override
    public Intent createLocalIntent() {
        return new Intent(EVENT_NAME);
    }
}
