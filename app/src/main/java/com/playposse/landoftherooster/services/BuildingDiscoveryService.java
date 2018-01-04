package com.playposse.landoftherooster.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

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

    private final Context context;

    private BuildingType nextBuildingType;
    private Integer nextDistance;

    public BuildingDiscoveryService(Context context) {
        this.context = context;

        initNextBuilding();
    }

    private void initNextBuilding() {
        Building lastBuilding = getLastBuilding(context);

        if (lastBuilding == null) {
            nextBuildingType = getNextBuildingType(context, INITIAL_BUILDING_TYPE);
        } else {
            nextBuildingType = getNextBuildingType(context, lastBuilding.getBuildingTypeId());
        }

        if (nextBuildingType != null) {
            Log.d(LOG_TAG, "initNextBuilding: The next building type is: "
                    + nextBuildingType.getName());
        } else {
            Log.d(LOG_TAG, "initNextBuilding: There are no more buildings to be discovered.");
        }
    }

    @Nullable
    public static BuildingType getNextBuildingType(Context context, int lastBuildingTypeId) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getNextBuildingType(lastBuildingTypeId);
    }

    @Nullable
    public static Building getLastBuilding(Context context) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getLastBuilding();
    }
}
