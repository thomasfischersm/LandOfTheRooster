package com.playposse.landoftherooster.services.combat;

import android.content.Context;

import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.broadcastintent.BattleAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

/**
 * Handler for the logic when a building with a potential battle is within range of the user.
 */
public final class BattleExecutor {

    private BattleExecutor() {}

    public static void promptUser(Context context, BuildingWithType buildingWithType) {
        RoosterBroadcastManager.send(
                context,
                new BattleAvailableBroadcastIntent(buildingWithType.getBuilding().getId()));
    }
}
