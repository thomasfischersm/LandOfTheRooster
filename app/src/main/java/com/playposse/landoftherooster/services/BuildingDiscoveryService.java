package com.playposse.landoftherooster.services;

import android.content.Context;
import android.location.Location;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;
import java.util.Random;

/**
 * A background service that discovers buildings.
 * <p>
 * <p>All buildings are discovered in sequence. A building is discovered when the user is at a
 * specific distance from the last discovered building. The specific distance is a randomly chosen
 * distance within the min/max range for that building type.
 */
public class BuildingDiscoveryService implements ILocationAwareService {

    private static final String LOG_TAG = BuildingDiscoveryService.class.getSimpleName();

    private static final int INITIAL_BUILDING_TYPE = 0;

    /**
     * An additional distance that the user can go while still being able to discover a building.
     * <p>
     * <p>Each building type has a min and a max. The discovery service decides on an actual
     * distance somewhere between those maximum. As long as the user has walked the distance, a
     * discovery happens. However, to prevent a really far building from all other buildings, a
     * safeguard prevents the discovery when the user exceeds the max distance.
     * <p>
     * <p>While that is good, if the actual distance and the max distance is very close, GPS
     * inaccuracy could make it hard to for the user to trigger. So a fudge factor is added to max
     * to ensure that a reasonable discovery is made.
     */
    private static final int MAX_GRACE_DISTANCE = 100;

    private final Context context;
    private final Random random = new Random();

    private BuildingType nextBuildingType;
    private Integer nextDistance;

    BuildingDiscoveryService(Context context) {
        this.context = context;

        initNextBuildingType();
    }

    private void initNextBuildingType() {
        Log.i(LOG_TAG, "initNextBuildingType: Start initNextBuildingType");
        Building lastBuilding = getLastBuilding(context);
        Log.i(LOG_TAG, "initNextBuildingType: Last building is of type " + lastBuilding);

        if (lastBuilding == null) {
            nextBuildingType = getNextBuildingType(context, INITIAL_BUILDING_TYPE);
            Log.i(LOG_TAG, "initNextBuildingType: Loaded initial building type "
                    + nextBuildingType);
        } else {
            nextBuildingType = getNextBuildingType(context, lastBuilding.getBuildingTypeId());
        }

        if ((nextBuildingType != null) && (nextBuildingType.getMinDistanceMeters() != null)) {
            Log.d(LOG_TAG, "initNextBuildingType: The next building type is: "
                    + nextBuildingType.getName());
            int min = nextBuildingType.getMinDistanceMeters();
            int delta = nextBuildingType.getMaxDistanceMeters()
                    - nextBuildingType.getMinDistanceMeters();
            nextDistance = min + random.nextInt(delta);
            Log.d(LOG_TAG, "initNextBuildingType: Distance to the next building is "
                    + nextDistance);
        } else {
            Log.d(LOG_TAG, "initNextBuildingType: There are no more buildings to be " +
                    "discovered.");
            nextDistance = null;
        }
        Log.i(LOG_TAG, "initNextBuildingType: End initNextBuildingType");
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
        if ((nextBuildingType == null) || (nextBuildingType.getMinDistanceMeters() != null)) {
            return;
        }

        placeNextBuilding(latLng);
    }

    private void placeNextBuilding(LatLng currentLatLng) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        // Check if we have a good GPS location.

        // Create the building.
        Building building = new Building(
                nextBuildingType.getId(),
                currentLatLng.latitude,
                currentLatLng.longitude);
        long buildingId = dao.insertBuilding(building);
        building.setId(buildingId);
        Log.d(LOG_TAG, "placeNextBuilding: Placed building: " + nextBuildingType.getName());

        // Create first resource/Unit if free.
        createFirstFreeItem(dao, building);

        // Prepare to place the next building.
        initNextBuildingType();

        // TODO: This will have to notify the activity to do something.
        // Maybe, the activity can simply observe the building table?
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void createFirstFreeItem(RoosterDao dao, Building building) {
        List<ProductionRule> productionRules =
                dao.getProductionRulesByBuildingTypeId(building.getBuildingTypeId());

        for (ProductionRule productionRule : productionRules) {
            if (!productionRule.isFree()) {
                continue;
            }

            Integer resourceTypeId = productionRule.getOutputResourceTypeId();
            if (resourceTypeId != null) {
                RoosterDaoUtil.creditResource(context, resourceTypeId, 1, building.getId());
            }

            Integer unitTypeId = productionRule.getOutputUnitTypeId();
            if (unitTypeId != null) {
                RoosterDaoUtil.creditUnit(context, unitTypeId, 1, building.getId());
            }
        }
    }

    private void checkIfBuildingDiscovered(LatLng currentLatLng) {
        Log.i(LOG_TAG, "checkIfBuildingDiscovered: Called with latitude: "
                + currentLatLng.latitude + " longitude: " + currentLatLng.longitude);

        if (nextBuildingType == null) {
            Log.d(LOG_TAG, "checkIfBuildingDiscovered: No more next building types to " +
                    "discover.");
            return;
        }

        Float distance = getMinDistanceFromCurrentBuildings(currentLatLng);
        Log.i(LOG_TAG, "checkIfBuildingDiscovered: Distance from nearest building: "
                + distance);

        if (distance == null) {
            Log.e(LOG_TAG, "checkIfBuildingDiscovered: Can't check because the distance is " +
                    "null!");
            return;
        }

        Integer limit = nextBuildingType.getMaxDistanceMeters() + MAX_GRACE_DISTANCE;
        Log.d(LOG_TAG, "checkIfBuildingDiscovered: Next building type requires "
                + nextDistance + " and no more than " + limit);
        if ((distance > nextDistance) && (distance < limit)) {
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
        Log.d(LOG_TAG, "getMinDistanceFromCurrentBuildings: Current location: "
                + "latitude: " + currentLatLng.latitude + " longitude: " + currentLatLng.longitude);

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

        Log.d(LOG_TAG, "getMinDistanceFromCurrentBuildings: Min distance from buildings: "
                + min);
        return min;
    }

    @Override
    public synchronized void onLocationUpdate(LatLng latLng) {
        handleFirstBuilding(latLng);
        checkIfBuildingDiscovered(latLng);
    }
}
