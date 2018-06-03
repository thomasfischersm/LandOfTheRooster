package com.playposse.landoftherooster.contentprovider.business.data;

import android.location.Location;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.List;

/**
 * A repository that keeps track if a user is inside of a building zone.
 */
public class BuildingZoneRepository {

    private static final String LOG_TAG = BuildingZoneRepository.class.getSimpleName();

    private static BuildingZoneRepository instance;

    private final RoosterDao dao;

    private BuildingWithType currentBuildingWithType;
    private LatLng currentLatLng;

    @VisibleForTesting
    BuildingZoneRepository(RoosterDao dao) {
        this.dao = dao;
    }

    public static BuildingZoneRepository get(RoosterDao dao) {
        if (instance == null) {
            instance = new BuildingZoneRepository(dao);
        }

        return instance;
    }

    public void updateLocation(LatLng latLng) {
        currentLatLng = latLng;

        Location currentLocation = new Location("");
        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);

        // Query the database.
        List<BuildingWithType> buildings = dao.getAllBuildingsWithType();

        // Calculate distances.
        for (BuildingWithType buildingWithType : buildings) {
            Building building = buildingWithType.getBuilding();

            Location buildingLocation = new Location("");
            buildingLocation.setLatitude(building.getLatitude());
            buildingLocation.setLongitude(building.getLongitude());

            float distance = currentLocation.distanceTo(buildingLocation);

            if (distance <= GameConfig.INTERACTION_RADIUS) {
                currentBuildingWithType = buildingWithType;
                return;
            }
        }

        // User is far from all buildings.
        currentBuildingWithType = null;
    }

    public BuildingWithType getCurrentBuildingWithType() {
        return currentBuildingWithType;
    }

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }
}
