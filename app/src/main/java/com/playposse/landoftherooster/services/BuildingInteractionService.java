package com.playposse.landoftherooster.services;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.Resource;
import com.playposse.landoftherooster.contentprovider.room.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

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

    private final Context context;

    BuildingInteractionService(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationUpdate(LatLng latLng) {
        BuildingWithType buildingWithType = findCurrentBuilding(latLng);
        if (buildingWithType == null) {
            return;
        }

        ResourceType producedResourceType = getProducedResourceType(buildingWithType);
        ResourceType precursorType = getPrecursorType(producedResourceType);

        if (producedResourceType == null) {
            Log.d(LOG_TAG, "onLocationUpdate: Building doesn't produce anything: "
                    + buildingWithType.getBuildingType().getName());
            return;
        } else if ((precursorType == null)) {
            Log.d(LOG_TAG, "onLocationUpdate: Building doesn't require precursor: "
                    + buildingWithType.getBuildingType().getName());

            if (!hasFreeStorageSlot()) {
                Log.d(LOG_TAG, "onLocationUpdate: The user has already filled the carrying " +
                        "capacity.");
                return;
            }

            creditResource(producedResourceType);
            return;
        }

        Resource precursorResource = getResource(precursorType);
        if ((precursorResource != null) && (precursorResource.getAmount() >= 1)) {
            Log.d(LOG_TAG, "onLocationUpdate: Regular building transaction.");
            creditResource(producedResourceType);
            debitResource(precursorResource);
        } else {
            Log.d(LOG_TAG, "onLocationUpdate: Building requires precursor: "
                    + buildingWithType.getBuildingType().getName()
                    + " that the user is missing: " + precursorType.getName());
        }
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

    @Nullable
    private ResourceType getProducedResourceType(BuildingWithType buildingWithType) {
        Integer producedResourceTypeId =
                buildingWithType.getBuildingType().getProducedResourceTypeId();

        if (producedResourceTypeId == null) {
            // Building doesn't produce anything.
            return null;
        }

        // Query the database.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getResourceTypeById(producedResourceTypeId);
    }

    @Nullable
    private ResourceType getPrecursorType(ResourceType producedResourceType) {
        if ((producedResourceType != null) && (producedResourceType.getPrecursorId() != null)) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            return dao.getResourceTypeById(producedResourceType.getPrecursorId());
        } else {
            return null;
        }
    }

    @Nullable
    private Resource getResource(ResourceType resourceType) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getResourceByTypeId(resourceType.getId());
    }

    private void creditResource(ResourceType producedResourceType) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource producedResource = dao.getResourceByTypeId(producedResourceType.getId());

        if (producedResource == null) {
            producedResource = new Resource(producedResourceType.getId(), 1);
            dao.insert(producedResource);
        } else {
            producedResource.setAmount(producedResource.getAmount() + 1);
            dao.update(producedResource);
        }
    }

    private void debitResource(Resource precursorResource) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        precursorResource.setAmount(precursorResource.getAmount() - 1);

        dao.update(precursorResource);
    }

    private boolean hasFreeStorageSlot() {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getResourceCount() <= 0;
    }
}
