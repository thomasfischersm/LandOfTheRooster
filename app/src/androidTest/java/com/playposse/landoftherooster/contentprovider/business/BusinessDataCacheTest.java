package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.RoosterApplication;
import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link BusinessDataCache}
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BusinessDataCacheTest extends TestData {

    private RoosterDao dao;
    private BusinessDataCache cache;
    private long wheatFieldId;

    @Before
    public void setUp() throws InterruptedException {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Wait for debug data to be complete.
        while (!RoosterApplication.isDebugDataComplete()) {
            Thread.sleep(10);
        }

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();

        // Create test data
        wheatFieldId = createWheatFieldAndMarker(dao);

        resetDataCache();
    }

    @After
    public void tearDown() {
        BuildingDiscoveryRepository.get(dao).reset();
    }

    @Test
    public void getBuildingWithType() {
        BuildingWithType buildingWithType = cache.getBuildingWithType();
        Building building = buildingWithType.getBuilding();

        assertEquals(wheatFieldId, cache.getBuildingId());
        assertNotNull(buildingWithType);
        assertEquals(wheatFieldId, building.getId());
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, building.getBuildingTypeId());
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, buildingWithType.getBuildingType().getId());
    }

    @Test
    public void getBuilding() {
        Building building = cache.getBuilding();

        assertEquals(wheatFieldId, cache.getBuildingId());
        assertNotNull(building);
        assertEquals(wheatFieldId, building.getId());
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, building.getBuildingTypeId());
    }

    @Test
    public void getBuildingType() {
        BuildingType buildingType = cache.getBuildingType();

        assertEquals(wheatFieldId, cache.getBuildingId());
        assertNotNull(buildingType);
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, buildingType.getId());
    }

    @Test
    public void getUnitsWithTypeJoiningUser() {
        // Assert clean test start.
        assertEquals(0, dao.getUnitsWithTypeJoiningUser().size());

        // Create a peasant and two soldiers joining the user.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 2, null);

        // Create unrelated units at buildings.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 3, wheatFieldId);
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 4, wheatFieldId);

        // Test code.
        List<UnitWithType> unitsWithType = cache.getUnitsWithTypeJoiningUser();

        // Assert outcome
        assertNotNull(unitsWithType);
        assertEquals(3, unitsWithType.size());
    }

    @Test
    public void hasInjuredUnitJoiningUser() {
        // Assert clean test start.
        assertEquals(0, dao.getUnitsWithTypeJoiningUser().size());
        assertFalse(cache.hasInjuredUnitJoiningUser());

        // Create a peasant and two soldiers joining the user.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, null);
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 2, null);

        // Create unrelated units at buildings.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 3, wheatFieldId);
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 4, wheatFieldId);

        // Test code.
        resetDataCache();
        assertFalse(cache.hasInjuredUnitJoiningUser());

        // Injure a unit.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        Unit unit = unitsWithType.get(0).getUnit();
        unit.setHealth(1);
        dao.update(unit);

        // Test code.
        assertFalse(cache.hasInjuredUnitJoiningUser()); // Ensure cache is accessed.
        resetDataCache();
        assertTrue(cache.hasInjuredUnitJoiningUser());
    }

    @Test
    public void getHealingUnitCountAndGetRecoveredUnitCount() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create a peasant joining the user and two soldiers at the hospital
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 1, hospitalId);
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 2, hospitalId);

        // Create unrelated units at buildings.
        RoosterDaoUtil.creditUnit(dao, SOLDIER_UNIT_TYPE_ID, 4, wheatFieldId);

        // Injure a unit.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeByBuildingId(hospitalId);
        Unit unit = unitsWithType.get(0).getUnit();
        unit.setHealth(1);
        dao.update(unit);

        // Create peasant joining the user.
        RoosterDaoUtil.creditUnit(dao, GameConfig.PEASANT_ID, 3, null);

        // Test.
        cache = new BusinessDataCache(dao, hospitalId);
        int healingUnitCount = cache.getHealingUnitCount();

        assertEquals(1, healingUnitCount);
        assertEquals(1, cache.getRecoveredUnitCount());
    }

    @Test
    public void getPeasantCount_default() {
        assertEquals(GameConfig.IMPLIED_PEASANT_COUNT, cache.getPeasantCount());
    }

    @Test
    public void getPeasantCount_additionalPeasant() {
        // Create peasant.
        createUnitsJoiningUser(dao, 1, GameConfig.PEASANT_ID);
        assertEquals(GameConfig.IMPLIED_PEASANT_COUNT, cache.getPeasantCount());

        // Assign peasant to wheat field.
        List<UnitWithType> unitsWithType = dao.getUnitsWithTypeJoiningUser();
        assertEquals(1, unitsWithType.size());
        Unit unit = unitsWithType.get(0).getUnit();
        unit.setLocatedAtBuildingId(wheatFieldId);
        dao.update(unit);

        // Test.
        resetDataCache();
        assertEquals(GameConfig.IMPLIED_PEASANT_COUNT + 1, cache.getPeasantCount());
    }

    private void resetDataCache() {
        cache = new BusinessDataCache(dao, wheatFieldId);
    }
}
