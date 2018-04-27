package com.playposse.landoftherooster.services.broadcastintent;

/**
 * Created by thoma on 4/6/2018.
 */
public class BuildingNeedsToRespawnBroadcastIntent extends AbstractBuildingBroadcastIntent {

    public BuildingNeedsToRespawnBroadcastIntent() {
    }

    public BuildingNeedsToRespawnBroadcastIntent(long buildingId) {
        super(buildingId);
    }
}
