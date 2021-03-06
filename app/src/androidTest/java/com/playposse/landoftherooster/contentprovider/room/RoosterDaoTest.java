package com.playposse.landoftherooster.contentprovider.room;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitCountByType;
import com.playposse.landoftherooster.util.SqliteUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.hasItems;

/**
 * A test for {@link RoosterDao}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoosterDaoTest {

    private static final String LOG_TAG = RoosterDaoTest.class.getSimpleName();

    private static final int RESOURCE_TYPE_ID = 1;
    private static final int RESOURCE_AMOUNT = 123;

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
    public void simpleDatabaseAccess() {
        // Insert resource.
        Resource resource = new Resource(RESOURCE_TYPE_ID, RESOURCE_AMOUNT, null);
        long resourceId = dao.insert(resource);
        Log.d(LOG_TAG, "simpleDatabaseAccess: Inserted a resource with id: " + resourceId);

        // Retrieve the resource.
        ResourceWithType resourceWithType = dao.getResourceWithTypeJoiningUser(1);

        assertNotNull(resourceWithType);
        assertEquals(RESOURCE_AMOUNT, resourceWithType.getResource().getAmount());
    }

    @Test
    public void getResourceCountJoiningUser() {
        int count = dao.getResourceCountJoiningUser();
        assertEquals(0, count);

        dao.insert(new Resource(1, 1, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(1, count);

        dao.insert(new Resource(2, 2, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(3, count);

        dao.insert(new Resource(2, 3, null));
        count = dao.getResourceCountJoiningUser();
        assertEquals(6, count);
    }

    @Test
    public void getUnitCountsJoiningUser() throws InterruptedException {
        // Wait for the test data to be finished creating. Then delete the test units.
        Thread.sleep(1_000);
        dao.deleteUnits();

        List<UnitCountByType> unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(0, unitCountList.size());

        dao.insert(new Unit(1, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(1, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());

        dao.insert(new Unit(2, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(1, unitCountList.get(1).getCount());

        dao.insert(new Unit(2, 10, null));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(2, unitCountList.get(1).getCount());

        dao.insert(new Unit(1, 10, 1L));
        unitCountList = dao.getUnitCountsJoiningUser();
        assertEquals(2, unitCountList.size());
        assertEquals(1, unitCountList.get(0).getUnitTypeId());
        assertEquals(1, unitCountList.get(0).getCount());
        assertEquals(2, unitCountList.get(1).getUnitTypeId());
        assertEquals(2, unitCountList.get(1).getCount());
    }

    @Test
    public void getCarryingCapacity() throws InterruptedException {
        // Wait for the test data to be finished creating. Then delete the test units.
        Thread.sleep(1_000);
        dao.deleteUnits();

        assertEquals(0, dao.getCarryingCapacity());

        // Add peasant.
        dao.insert(new Unit(1, 10, null));
        assertEquals(1, dao.getCarryingCapacity());

        // Add soldier.
        dao.insert(new Unit(2, 10, null));
        assertEquals(1, dao.getCarryingCapacity());

        // Add peasant to building.
        dao.insert(new Unit(1, 10, 1L));
        assertEquals(1, dao.getCarryingCapacity());

        // Add second peasant.
        dao.insert(new Unit(1, 10, null));
        assertEquals(2, dao.getCarryingCapacity());
    }

    @Test
    public void getMapMarkersOfHealingBuildings() {
        // Create 3 non-hospital buildings.
        TestData.createWheatFieldAndMarker(dao);
        TestData.createMillAndMarker(dao);
        TestData.createBakeryAndMarker(dao);

        // Create 2 hospitals.
        long hospitalId0 = TestData.createHospitalAndMarker(dao);
        long hospitalId1 = TestData.createHospitalAndMarker(dao);

        // Query dao.
        List<MapMarker> mapMarkers = dao.getMapMarkersOfHealingBuildings();

        // Assert result.
        assertEquals(2, mapMarkers.size());

        // Assert building ids.
        List<Long> buildingIds = new ArrayList<>();
        for (MapMarker mapMarker : mapMarkers) {
            buildingIds.add(mapMarker.getBuildingId());
        }
        assertThat(buildingIds, hasItems(hospitalId0, hospitalId1));
    }

    @Test
    public void explain_getUnitsWithTypeJoiningUser() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        String sql = "explain select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc";
        SqliteUtil.explain(targetContext, sql);
    }

    @Test
    public void explain_getWoundedUnitsWithTypeJoiningUser() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        String sql = "explain select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null and unit.health < unit_type.health order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc";
        SqliteUtil.explain(targetContext, sql);
    }
}
