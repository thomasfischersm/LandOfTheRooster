package com.playposse.landoftherooster.services;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.broadcastintent.BuildingAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.BuildingNeedsToRespawnBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.LeftBuildingBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;

import java.util.Date;
import java.util.List;

/**
 * A service that detects when the user walks into the area of an existing building. It enables the
 * user to do the functions that the building offers. In particularly, it converts one resource into
 * another, e.g. a bakery converts flour into bread.
 */
public class BuildingInteractionService implements ILocationAwareService {

    private static final String LOG_TAG = BuildingInteractionService.class.getSimpleName();

    /**
     * The radius in meters within a building can be interacted with. This counteracts the
     * inaccuracy of the GPS location.
     */
    private static final int INTERACTION_RADIUS = 30;

    /**
     * Time in ms that it takes for a battle building to respawn units.
     */
    private static final int RESPAWN_DURATION = 24 * 60 * 60 * 1_000;

    private final Context context;

    private BuildingWithType currentBuildingWithType = null;

    BuildingInteractionService(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationUpdate(LatLng latLng) {
        BuildingWithType buildingWithType = findCurrentBuilding(latLng);
        if (buildingWithType == null) {
            if (currentBuildingWithType != null) {
                currentBuildingWithType = null;
                RoosterBroadcastManager.send(context, new LeftBuildingBroadcastIntent());
            }
        } else if (currentBuildingWithType == null) {
            currentBuildingWithType = buildingWithType;

            BuildingType buildingType = buildingWithType.getBuildingType();

            // Show user the building resources dialog.
            RoosterBroadcastManager.send(
                    context,
                    new BuildingAvailableBroadcastIntent(buildingWithType.getBuilding().getId()));

            // Try fighting.
            if (buildingType.getEnemyUnitCount() != null) {
                onFoundBattleBuilding(buildingWithType);
            }
        }
    }

    private void onFoundBattleBuilding(BuildingWithType buildingWithType) {
        // Reset last conquest date if necessary
        Building building = buildingWithType.getBuilding();
        Date lastConquest = building.getLastConquest();
        if (lastConquest != null) {
            long lastConquestMs = lastConquest.getTime();
            if (lastConquestMs + RESPAWN_DURATION > System.currentTimeMillis()) {
                // Building has not yet re-spawned.
                long remainingMs = lastConquestMs + RESPAWN_DURATION - System.currentTimeMillis();
                RoosterBroadcastManager.send(
                        context,
                        new BuildingNeedsToRespawnBroadcastIntent(remainingMs));
                return;
            } else {
                // Respawn building.
                building.setLastConquest(null);
                RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
                dao.update(building);
            }
        }

        BattleExecutor.promptUser(context, buildingWithType);
    }

    @Nullable
    private BuildingWithType findCurrentBuilding(LatLng currentLatLng) {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatLng.latitude);
        currentLocation.setLongitude(currentLatLng.longitude);

        // Query the database.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        List<BuildingWithType> buildings = dao.getAllBuildingsWithType();

        // Calculate distances.
        for (BuildingWithType buildingWithType : buildings) {
            Building building = buildingWithType.getBuilding();

            Location buildingLocation = new Location("");
            buildingLocation.setLatitude(building.getLatitude());
            buildingLocation.setLongitude(building.getLongitude());

            float distance = currentLocation.distanceTo(buildingLocation);

            if (distance <= INTERACTION_RADIUS) {
                return buildingWithType;
            }
        }

        // User is far from all buildings.
        return null;
    }
}
