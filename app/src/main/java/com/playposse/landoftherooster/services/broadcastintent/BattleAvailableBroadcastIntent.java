package com.playposse.landoftherooster.services.broadcastintent;

import android.content.Intent;

/**
 * A broadcast {@link Intent} that tells the map activity that the user can engage in a battle.
 */
public class BattleAvailableBroadcastIntent extends AbstractBuildingBroadcastIntent {

    public BattleAvailableBroadcastIntent() {
    }

    public BattleAvailableBroadcastIntent(long buildingId) {
        super(buildingId);
    }
}
