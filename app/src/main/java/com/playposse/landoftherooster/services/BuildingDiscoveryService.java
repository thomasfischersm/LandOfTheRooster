package com.playposse.landoftherooster.services;

import android.content.Context;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import java.util.List;

/**
 * A background service that discovers buildings.
 *
 * <p>All buildings are discovered in sequence. A building is discovered when the user is at a
 * specific distance from the last discovered building. The specific distance is a randomly chosen
 * distance within the min/max range for that building type.
 */
public class BuildingDiscoveryService {

    private static final String LOG_TAG = BuildingDiscoveryService.class.getSimpleName();

    private BuildingType nextBuildingType;
    private Integer nextDistance;

    private void initNextBuilding() {

    }

    public static BuildingType getNextBuildingTypeadsf(Context context) {
        List<BuildingType> buildingTypes =
                RoosterDatabase.getInstance(context).getDao().getAllBuildingTypes();
        Log.d(LOG_TAG, "getNextBuildingType: Got building types " + buildingTypes.size());
        return null;
    }
}
