package com.playposse.landoftherooster.contentprovider.business.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;

/**
 * A test for {@link BuildingRepository}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BuildingRepositoryTest {

    private static final double EPSILON = 0.0001;

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

        // Reset BuildingRepository
        BuildingRepository.get(dao)
                .stop();
    }

    @After
    public void tearUp() {
        // Reset BuildingRepository
        BuildingRepository.get(dao)
                .stop();
    }

    @Test
    public void getBuildingById_initialization() {
        // Create building.
        long wheatFieldId = TestData.createWheatField(dao);

        // Check repository.
        BuildingRepository buildingRepository = BuildingRepository.get(dao);
        Building building = buildingRepository.getBuildingById(wheatFieldId);

        // Assert outcome.
        assertNotNull(building);
        assertEquals(wheatFieldId, building.getId());
        assertEquals(TestData.WHEAT_FIELD_BUILDING_TYPE_ID, building.getBuildingTypeId());
    }

    @Test
    public void getBuildingById_eventListener() {
        // Start repository.
        BuildingRepository buildingRepository = BuildingRepository.get(dao);

        // Create new building.
        Building createdBuilding = new Building(
                TestData.WHEAT_FIELD_BUILDING_TYPE_ID,
                TestData.LATITUDE,
                TestData.LONGITUDE);
        DaoEventRegistry.get(dao)
                .insert(createdBuilding);

        // Check repository for building.
        Building retrievedBuilding = buildingRepository.getBuildingById(createdBuilding.getId());

        // Assert outcome.
        assertNotNull(retrievedBuilding);
        assertNotSame(createdBuilding, retrievedBuilding);
        assertEquals(createdBuilding.getId(), retrievedBuilding.getId());
        assertEquals(TestData.WHEAT_FIELD_BUILDING_TYPE_ID, retrievedBuilding.getBuildingTypeId());
        assertEquals(TestData.LATITUDE, retrievedBuilding.getLatitude(), EPSILON);

        // Update the building.
        retrievedBuilding.setLatitude(0);
        DaoEventRegistry.get(dao)
                .update(retrievedBuilding);

        // Check repository for building.
        Building thirdBuilding = buildingRepository.getBuildingById(createdBuilding.getId());

        // Assert outcome.
        assertNotNull(thirdBuilding);
        assertNotSame(createdBuilding, thirdBuilding);
        assertEquals(createdBuilding.getId(), thirdBuilding.getId());
        assertEquals(TestData.WHEAT_FIELD_BUILDING_TYPE_ID, thirdBuilding.getBuildingTypeId());
        assertEquals(0.0, thirdBuilding.getLatitude());
    }

    /**
     * Tests that the repository hits the cache and doesn't load from the database each time.
     */
    @Test
    public void getBuildingById_cache() {
        // Initialize cache with an empty table.
        BuildingRepository buildingRepository = BuildingRepository.get(dao);

        // Create a building bypassing the cache.
        long wheatFieldId = TestData.createWheatField(dao);

        // Check that the building is missing from the cache.
        Building building = buildingRepository.getBuildingById(wheatFieldId);

        assertNull(building);
    }
}
