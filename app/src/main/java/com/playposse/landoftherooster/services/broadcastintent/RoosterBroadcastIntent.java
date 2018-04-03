package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * An interface for broadcast intents that are based on intents from a service to the map activity.
 * In response, the map activity will offer the user some option or message.
 */
public interface RoosterBroadcastIntent {

    void createFromIntent(Intent localIntent);
    Intent createLocalIntent();
}
