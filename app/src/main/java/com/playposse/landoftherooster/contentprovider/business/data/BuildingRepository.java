package com.playposse.landoftherooster.contentprovider.business.data;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventObserver;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.android.gms.common.internal.Preconditions.checkNotNull;

/**
 * A repository that caches inserts and updates to {@link Building}, so that queries can be served
 * from memory. The database access time can be 20-30 ms. The access time to a hash map should be
 * 0 ms. Multiple database calls can add up to some {@link BusinessEvent}s taking well over 100ms.
 * This particularly adds up for running the whole test suite.
 */
public final class BuildingRepository {

    private static final String LOG_TAG = BuildingTypeRepository.class.getSimpleName();

    private static BuildingRepository instance;

    private final RoosterDao dao;
    private final Map<Long, Building> idToBuildingMap = new HashMap<>();

    private LocalDaoEventObserver daoEventObserver = new LocalDaoEventObserver();

    private BuildingRepository(RoosterDao dao) {
        checkNotNull(dao);

        this.dao = dao;

        init();
    }

    public synchronized static BuildingRepository get(RoosterDao dao) {
        checkNotNull(dao);

        if (instance == null) {
            instance = new BuildingRepository(dao);
        }
        return instance;
    }

    private void init() {
        // Read existing buildings from the database.
        List<Building> buildings = dao.getAllBuildings();
        for (Building building : buildings) {
            idToBuildingMap.put(building.getId(), new Building(building));
        }

        // Listen to updates, inserts, and deletes for future changes.
        DaoEventRegistry.get(dao)
                .registerObserver(daoEventObserver);
    }

    public synchronized static void stop() {
        if (instance != null) {
            BuildingRepository localInstance = BuildingRepository.instance;
            BuildingRepository.instance = null;
            DaoEventRegistry.get(localInstance.dao)
                    .unregisterObserver(localInstance.daoEventObserver);
        }
    }

    @Nullable
    public Building getBuildingById(long buildingId) {
        Building building = idToBuildingMap.get(buildingId);
        return (building != null) ? new Building(building) : null;
    }

    /**
     * Finds the building with the highest id.
     */
    @Nullable
    public Building getLastBuilding() {
        if (idToBuildingMap.size() == 0) {
            return null;
        }

        Building lastBuilding = null;
        for (Building building : idToBuildingMap.values()) {
            if (lastBuilding == null) {
                lastBuilding = building;
            } else if (lastBuilding.getId() < building.getId()) {
                lastBuilding = building;
            }
        }

        return (lastBuilding != null) ? new Building(lastBuilding) : null;
    }

    public List<Building> getAllBuildings() {
        ArrayList<Building> buildings = new ArrayList<>(idToBuildingMap.values());

        // Make defensive building copies.
        for (int i = 0; i < buildings.size(); i++) {
            buildings.set(i, new Building(buildings.get(i)));
        }

        return buildings;
    }

    /**
     * A {@link DaoEventObserver} that listens to updates, inserts, and deletes of buildings.
     */
    private class LocalDaoEventObserver implements DaoEventObserver {

        @Override
        public void onBuildingModified(Building building, EventType eventType) {
            switch (eventType) {
                case INSERT:
                    idToBuildingMap.put(building.getId(), new Building(building));
                    break;
                case UPDATE:
                    idToBuildingMap.put(building.getId(), new Building(building));
                    break;
                case DELETE:
                    idToBuildingMap.remove(building.getId());
                    break;
            }
        }

        @Override
        public void onResourceModified(Resource resource, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onMapMarkerModified(MapMarker mapMarker, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onResourceLocationUpdated(
                Resource resource,
                @Nullable Long beforeBuildingId,
                @Nullable Long afterBuildingId) {

            // Ignore.
        }

        @Override
        public void onUnitModified(Unit unit, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onUnitLocationUpdated(
                Unit unit,
                @Nullable Long beforeBuildingId,
                @Nullable Long afterBuildingId) {

            // Ignore.
        }
    }
}
