package com.playposse.landoftherooster.contentprovider.room.event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.util.MutableLong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link DaoEventRegistry}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DaoEventRegistryTest {

    private final static long BUILDING_ID = 1;

    private RoosterDao dao;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Clear data that test may generate.
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();
    }

    @Test
    public void insertBuilding() {
        MutableLong counter = registerBuildingObserver();

        DaoEventRegistry.get(dao).insert(new Building(BUILDING_ID, 1, 2));

        assertEquals(1, counter.getValue());
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
