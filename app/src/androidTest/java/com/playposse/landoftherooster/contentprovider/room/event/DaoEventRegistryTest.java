package com.playposse.landoftherooster.contentprovider.room.event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.util.MutableLong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link DaoEventRegistry}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DaoEventRegistryTest {

    private final static long BUILDING_ID = 1;
    private final static long BUILDING_TYPE_ID = 1;

    private RoosterDao dao;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();
    }

    @Test
    public void insertBuilding() {
        final MutableLong reportedBuildingId = new MutableLong(-1);
        DaoEventRegistry.get(dao).registerObserver(
                new DaoEventObserver() {
                    @Override
                    public void onBuildingModified(Building building, EventType eventType) {
                        reportedBuildingId.setValue(building.getId());
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
                });

        Building building = new Building(BUILDING_TYPE_ID, 1, 2);
        long buildingId =DaoEventRegistry.get(dao).insert(building);

        assertEquals(buildingId, reportedBuildingId.getValue());
    }

    @Test
    public void insertResourceAtBuilding() {
        MutableLong counter = registerBuildingObserver();

        DaoEventRegistry.get(dao).insert(new Resource(1L, 1, BUILDING_ID));

        assertEquals(1, counter.getValue());
    }

    @Test
    public void updateLocationOfResource() {
        MutableLong counter = registerBuildingObserver();

        // Create initial resource with user.
        Resource resource = new Resource(1L, 1, null);
        DaoEventRegistry.get(dao).insert(resource);
        assertEquals(0, counter.getValue());

        // Move resource to building.
        resource.setLocatedAtBuildingId(BUILDING_ID);
        DaoEventRegistry.get(dao).updateLocation(resource, null, BUILDING_ID);
        assertEquals(1, counter.getValue());

        // Move resource away from building.
        resource.setLocatedAtBuildingId(null);
        DaoEventRegistry.get(dao).updateLocation(resource, BUILDING_ID, null);
        assertEquals(2, counter.getValue());
    }

    @NonNull
    private MutableLong registerBuildingObserver() {
        final MutableLong counter = new MutableLong(0);

        // TODO: Could be nice and clean up the observer.
        DaoEventRegistry.get(dao).registerObserver(new BuildingSpecificEventObserver(BUILDING_ID) {
            @Override
            protected void onRelevantBuildingUpdate(long buildingId) {
                counter.add(1);
            }
        });
        return counter;
    }
}
