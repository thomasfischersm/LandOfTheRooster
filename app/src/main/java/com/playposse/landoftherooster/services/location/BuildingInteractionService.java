package com.playposse.landoftherooster.services.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;
import com.playposse.landoftherooster.services.broadcastintent.BuildingAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.BuildingNeedsToRespawnBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.HospitalAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.LeftBuildingBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;
import com.playposse.landoftherooster.services.combat.BattleExecutor;

import java.util.Date;
import java.util.List;

import static com.playposse.landoftherooster.GameConfig.BATTLE_RESPAWN_DURATION;
import static com.playposse.landoftherooster.GameConfig.INTERACTION_RADIUS;

/**
 * A service that detects when the user walks into the area of an existing building. It enables the
 * user to do the functions that the building offers. In particularly, it converts one resource into
 * another, e.g. a bakery converts flour into bread.
 */
public class BuildingInteractionService implements ILocationAwareService {

    private static final String LOG_TAG = BuildingInteractionService.class.getSimpleName();

    private final Context context;

    private BuildingWithType currentBuildingWithType = null;

    public BuildingInteractionService(Context context) {
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

            if (buildingType.getEnemyUnitCount() != null) {
                // Try fighting.
                onFoundBattleBuilding(buildingWithType);
            } else if (buildingType.isHealsUnits()) {
                // Show hospital dialog.
                RoosterBroadcastManager.send(
                        context,
                        new HospitalAvailableBroadcastIntent(buildingWithType.getBuilding().getId()));
            } else {
                // Show user the building resources dialog.
                RoosterBroadcastManager.send(
                        context,
                        new BuildingAvailableBroadcastIntent(buildingWithType.getBuilding().getId()));
            }
        }
    }

    private void onFoundBattleBuilding(BuildingWithType buildingWithType) {
        // Reset last conquest date if necessary
        Building building = buildingWithType.getBuilding();
        Date lastConquest = building.getLastConquest();
        if (lastConquest != null) {
            long lastConquestMs = lastConquest.getTime();
            if (lastConquestMs + BATTLE_RESPAWN_DURATION > System.currentTimeMillis()) {
                // Building has not yet re-spawned.
                long buildingId = buildingWithType.getBuilding().getId();
                RoosterBroadcastManager.send(
                        context,
                        new BuildingNeedsToRespawnBroadcastIntent(buildingId));
                return;
            } else {
                // Respawn building.
                building.setLastConquest(null);
                RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
                DaoEventRegistry.get(dao).update(building);
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
