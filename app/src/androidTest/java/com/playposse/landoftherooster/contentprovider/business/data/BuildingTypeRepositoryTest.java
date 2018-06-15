package com.playposse.landoftherooster.contentprovider.business.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;
import com.playposse.landoftherooster.util.DoubleUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * A test for {@link BuildingTypeRepository}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BuildingTypeRepositoryTest {

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
        BuildingRepository.stop();
    }

    @After
    public void tearUp() {
        // Reset BuildingRepository
        BuildingRepository.stop();
    }

    @Test
    public void getAllBuildingsWithType() {
        // Assert initial state.
        BuildingTypeRepository repository = BuildingTypeRepository.get(dao);
        assertEquals(0, repository.getAllBuildingsWithType().size());

        // Create first building.
        Building wheatField = new Building(
                TestData.WHEAT_FIELD_BUILDING_TYPE_ID,
                TestData.LATITUDE,
                TestData.LONGITUDE);
        DaoEventRegistry.get(dao).insert(wheatField);

        // Assert outcome.
        List<BuildingWithType> buildingsWithType = repository.getAllBuildingsWithType();
        assertEquals(1, buildingsWithType.size());

        BuildingWithType resultBuildingWithType = buildingsWithType.get(0);
        Building resultBuilding = resultBuildingWithType.getBuilding();
        assertEquals(wheatField.getId(), resultBuilding.getId());
        assertEquals(wheatField.getBuildingTypeId(), resultBuilding.getBuildingTypeId());
        assertEquals(TestData.LATITUDE, resultBuilding.getLatitude(), DoubleUtil.EPSILON);
        assertEquals(TestData.LONGITUDE, resultBuilding.getLongitude(), DoubleUtil.EPSILON);

        BuildingType resultBuildingType = resultBuildingWithType.getBuildingType();
        assertEquals(TestData.WHEAT_FIELD_BUILDING_TYPE_ID, resultBuildingType.getId());

        // Create a second building.
        Building mill = new Building(
                TestData.MILL_BUILDING_TYPE_ID,
                TestData.LATITUDE,
                TestData.LONGITUDE);
        DaoEventRegistry.get(dao).insert(mill);
        assertEquals(2, repository.getAllBuildingsWithType().size());
    }
}
