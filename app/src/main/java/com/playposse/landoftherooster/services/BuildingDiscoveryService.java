package com.playposse.landoftherooster.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.ConvenientLocationProvider;

/**
 * A background service that discovers buildings.
 *
 * <p>All buildings are discovered in sequence. A building is discovered when the user is at a
 * specific distance from the last discovered building. The specific distance is a randomly chosen
 * distance within the min/max range for that building type.
 */
public class BuildingDiscoveryService {

    private static final String LOG_TAG = BuildingDiscoveryService.class.getSimpleName();

    private static final int INITIAL_BUILDING_TYPE = 0;
    private static final int LOCATION_CHECK_INTERVAL = 1_000;

    private final Context context;
    private final ConvenientLocationProvider convenientLocationProvider;

    private BuildingType nextBuildingType;
    private Integer nextDistance;

    public BuildingDiscoveryService(Context context) {
        this.context = context;

        initNextBuildingType();

        convenientLocationProvider = new ConvenientLocationProvider(
                context,
                LOCATION_CHECK_INTERVAL,
                new LocationCallback());
    }

    private void initNextBuildingType() {
        Building lastBuilding = getLastBuilding(context);

        if (lastBuilding == null) {
            nextBuildingType = getNextBuildingType(context, INITIAL_BUILDING_TYPE);
        } else {
            nextBuildingType = getNextBuildingType(context, lastBuilding.getBuildingTypeId());
        }

        if (nextBuildingType != null) {
            Log.d(LOG_TAG, "initNextBuildingType: The next building type is: "
                    + nextBuildingType.getName());
        } else {
            Log.d(LOG_TAG, "initNextBuildingType: There are no more buildings to be discovered.");
        }
    }

    @Nullable
    private static BuildingType getNextBuildingType(Context context, int lastBuildingTypeId) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getNextBuildingType(lastBuildingTypeId);
    }

    @Nullable
    private static Building getLastBuilding(Context context) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getLastBuilding();
    }

    private void handleFirstBuilding(LatLng latLng) {
        if (nextBuildingType.getMinDistanceMeters() != 0) {
            return;
        }

        placeNextBuilding(latLng);
    }

    private void placeNextBuilding(LatLng latLng) {
        // Check if we have a good GPS location.

        // Create the building.
        Building building =
                new Building(nextBuildingType.getId(), latLng.latitude, latLng.longitude);
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        dao.insertBuilding(building);

        // Prepare to place the next building.
        initNextBuildingType();
    }

    private void checkIfBuildingDiscovered(LatLng latLng) {
        // TODO
    }

    /**
     * Callback that gets called when a new GPS location becomes available. At that time, new
     * buildings are created if the user is ready.
     */
    private class LocationCallback implements ConvenientLocationProvider.Callback {

        @Override
        public void onNewLocation(LatLng latLng) {
            handleFirstBuilding(latLng);
            checkIfBuildingDiscovered(latLng);
        }

        @Override
        public void onMissingPermission() {
            Log.e(LOG_TAG, "onMissingPermission: Cannot run BuildingDiscoveryService due to " +
                    "a lack of permission.");
        }
    }
}
