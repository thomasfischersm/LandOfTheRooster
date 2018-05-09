package com.playposse.landoftherooster.contentprovider.business.data;

import android.location.Location;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

/**
 * A singleton class that keeps track of which building has been discovered.
 */
public class BuildingDiscoveryRepository {

    private static final String LOG_TAG = BuildingDiscoveryRepository.class.getSimpleName();

    private static BuildingDiscoveryRepository instance;

    private final Random random = new Random();
    private final RoosterDao dao;

    private BuildingType nextBuildingType;
    private boolean hasMoreBuildingTypes = true;
    @Nullable private Integer nextDistance;


    private BuildingDiscoveryRepository(RoosterDao dao) {
        this.dao = dao;

        findInitialBuilding();
    }

    public static BuildingDiscoveryRepository get(RoosterDao dao) {
        if (instance == null) {
            instance = new BuildingDiscoveryRepository(dao);
        }

        return instance;
    }

    private void findInitialBuilding() {
        Building lastBuilding = dao.getLastBuilding();

        if (lastBuilding == null) {
            // This is a new game install.
            long nextBuildingTypeId = GameConfig.INITIAL_BUILDING_TYPE_ID;
            nextBuildingType = dao.getBuildingType(nextBuildingTypeId);
            nextDistance = 0;
        } else {
            // This is an app restart.
            long lastBuildingTypeId = lastBuilding.getBuildingTypeId();
            moveToNextBuildingType(lastBuildingTypeId);
        }
    }

    public BuildingType getNextBuildingType() {
        return nextBuildingType;
    }

    public void moveToNextBuildingType() {
        moveToNextBuildingType(nextBuildingType.getId());
    }

    private void moveToNextBuildingType(long currentBuildingTypeId) {
        if (!hasMoreBuildingTypes) {
            return;
        }

        nextBuildingType = dao.getNextBuildingType(currentBuildingTypeId);
        hasMoreBuildingTypes = (nextBuildingType != null);

        if (nextBuildingType == null) {
            nextDistance = null;
            return;
        }

        // Calculate distance to the next building.
        Integer min = nextBuildingType.getMinDistanceMeters();
        Integer max = nextBuildingType.getMaxDistanceMeters();

        if ((min == null) || (max == null)) {
            // The initial building is discovered instantly.
            nextDistance = 0;
        } else {
            int delta = max - min;
            nextDistance = min + random.nextInt(delta);
        }
    }
    @Nullable
    public Float getMinDistanceFromCurrentBuildings(LatLng currentLatLng) {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatLng.latitude);
        currentLocation.setLongitude(currentLatLng.longitude);
        Log.d(LOG_TAG, "getMinDistanceFromCurrentBuildings: Current location: "
                + "latitude: " + currentLatLng.latitude + " longitude: " + currentLatLng.longitude);

        Float min = null;
        Float max = null; // Gather for future use.

        // Query db.
        List<Building> buildings = dao.getAllBuildings();

        // Iterate over buildings and collect min/max
        for (Building building : buildings) {
            Location buildingLocation = new Location("");
            buildingLocation.setLatitude(building.getLatitude());
            buildingLocation.setLongitude(building.getLongitude());

            float distance = currentLocation.distanceTo(buildingLocation);

            if ((min == null) || (max == null)) {
                min = distance;
                max = distance;
            } else {
                min = Math.min(min, distance);
                max = Math.max(max, distance);
            }
        }

        Log.d(LOG_TAG, "getMinDistanceFromCurrentBuildings: Min distance from buildings: "
                + min);
        return min;
    }

    @Nullable
    public Integer getNextDistance() {
        return nextDistance;
    }

    public boolean isHasMoreBuildingTypesToDiscover() {
        return hasMoreBuildingTypes;
    }

    @VisibleForTesting
    public void reset() {
        instance = null;
    }
}
