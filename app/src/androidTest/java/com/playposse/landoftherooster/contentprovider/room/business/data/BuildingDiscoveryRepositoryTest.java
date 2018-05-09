package com.playposse.landoftherooster.contentprovider.room.business.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link BuildingDiscoveryRepository}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BuildingDiscoveryRepositoryTest {

    private static final int WHEAT_FIELD_BUILDING_TYPE_ID = 2;
    private static final int MILL_BUILDING_TYPE_ID = 3;

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

    @After
    public void tearDown() {
        BuildingDiscoveryRepository.get(dao).reset();
    }

    @Test
    public void getNextBuildingType() {
        BuildingType buildingType = BuildingDiscoveryRepository.get(dao).getNextBuildingType();
        assertNotNull(buildingType);
        assertEquals(GameConfig.INITIAL_BUILDING_TYPE_ID, buildingType.getId());
    }

    @Test
    public void moveToNextBuildingType() {
        // Check initial state.
        BuildingType buildingType = BuildingDiscoveryRepository.get(dao).getNextBuildingType();
        assertNotNull(buildingType);
        assertEquals(GameConfig.INITIAL_BUILDING_TYPE_ID, buildingType.getId());

        // Move to second building type.
        BuildingDiscoveryRepository.get(dao).moveToNextBuildingType();
        buildingType = BuildingDiscoveryRepository.get(dao).getNextBuildingType();
        assertNotNull(buildingType);
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, buildingType.getId());

        // Move to the third building type.
        BuildingDiscoveryRepository.get(dao).moveToNextBuildingType();
        buildingType = BuildingDiscoveryRepository.get(dao).getNextBuildingType();
        assertNotNull(buildingType);
        assertEquals(MILL_BUILDING_TYPE_ID, buildingType.getId());
    }

    @Test
    public void getNextDistance() {
        // Initial building distance should be 0.
        BuildingDiscoveryRepository repository = BuildingDiscoveryRepository.get(dao);
        Integer nextDistance = repository.getNextDistance();
        assertNotNull(nextDistance);
        assertEquals(0, (int) nextDistance);

        // The second building distance should be within the expected range.
        repository.moveToNextBuildingType();
        nextDistance = repository.getNextDistance();
        assertNotNull(nextDistance);
        assertTrue(nextDistance >= 110);
        assertTrue(nextDistance <= 160 + GameConfig.MAX_GRACE_DISTANCE);

        // The same for the third building.
        repository.moveToNextBuildingType();
        nextDistance = repository.getNextDistance();
        assertNotNull(nextDistance);
        assertTrue(nextDistance >= 110);
        assertTrue(nextDistance <= 160 + GameConfig.MAX_GRACE_DISTANCE);

    }
}
