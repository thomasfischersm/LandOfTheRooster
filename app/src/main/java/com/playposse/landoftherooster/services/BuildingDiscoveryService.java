package com.playposse.landoftherooster.services;

import android.content.Context;
import android.location.Location;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.ConvenientLocationProvider;

import java.util.List;
import java.util.Random;

/**
 * A background service that discovers buildings.
 * <p>
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
    private final Random random = new Random();

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
            int min = nextBuildingType.getMinDistanceMeters();
            int delta = nextBuildingType.getMaxDistanceMeters()
                    - nextBuildingType.getMinDistanceMeters();
            nextDistance = min + random.nextInt(delta);
        } else {
            Log.d(LOG_TAG, "initNextBuildingType: There are no more buildings to be " +
                    "discovered.");
            nextDistance = null;
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

    private void placeNextBuilding(LatLng currentLatLng) {
        // Check if we have a good GPS location.

        // Create the building.
        Building building =
                new Building(nextBuildingType.getId(), currentLatLng.latitude, currentLatLng.longitude);
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        dao.insertBuilding(building);

        // Prepare to place the next building.
        initNextBuildingType();

        // TODO: This will have to notify the activity to do something.
        // Maybe, the activity can simply observe the building table?
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void checkIfBuildingDiscovered(LatLng currentLatLng) {
        if (nextBuildingType == null) {
            Log.d(LOG_TAG, "checkIfBuildingDiscovered: No more next building types to " +
                    "discover.");
        }

        Float distance = getMinDistanceFromCurrentBuildings(currentLatLng);

        if (distance == null) {
            Log.e(LOG_TAG, "checkIfBuildingDiscovered: Can't check because the distance is " +
                    "null!");
        }

        if ((distance > nextDistance) && (distance < nextBuildingType.getMaxDistanceMeters())) {
            Log.d(LOG_TAG, "checkIfBuildingDiscovered: Discovered the next building: "
                    + nextBuildingType.getName());
            placeNextBuilding(currentLatLng);
        }
    }

    @Nullable
    private Float getMinDistanceFromCurrentBuildings(LatLng currentLatLng) {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatLng.latitude);
        currentLocation.setLongitude(currentLatLng.longitude);

        Float min = null;
        Float max = null; // Gather for future use.

        // Query db.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
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

        return min;
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
