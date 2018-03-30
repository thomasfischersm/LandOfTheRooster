package com.playposse.landoftherooster.services;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingWithType;
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

        ProductionExecutor.produce(context, buildingWithType);
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
