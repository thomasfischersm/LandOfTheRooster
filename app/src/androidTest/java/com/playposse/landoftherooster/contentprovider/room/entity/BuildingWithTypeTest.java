package com.playposse.landoftherooster.contentprovider.room.entity;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.RoosterApplication;
import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingTypeRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.SqliteUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A test used to profile the db access for {@link BuildingWithType}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BuildingWithTypeTest extends TestData {

    private static final String LOG_TAG = BuildingWithTypeTest.class.getSimpleName();

    private static final int REPETITION_COUNT = 1_000;

    private static long wheatFieldId;
    private static RoosterDao dao;

    private long start;

    @BeforeClass
    public static void setUp() throws InterruptedException {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();
        Log.i(LOG_TAG, "setUp: got dao " + dao);

        // Wait for debug data to be complete.
        while (!RoosterApplication.isDebugDataComplete()) {
            Thread.sleep(10);
        }

        // Clear data that test may generate.
        dao.deleteBuildings();

        wheatFieldId = createWheatField(dao);
    }

    @Test
    public void load_Building() {
        trace(
                "Load Building through dao",
                new Runnable() {
                    @Override
                    public void run() {
                        dao.getBuildingById(wheatFieldId);
                    }
                }
        );
    }

    @Test
    public void load_Building_explicitColumns() {
        trace(
                "Load Building through dao with explicit columns",
                new Runnable() {
                    @Override
                    public void run() {
                        dao.getBuildingByIdDDD(wheatFieldId);
                    }
                }
        );
    }

    @Test
    public void load_BuildingWithType() {
        trace(
                "Load BuildingWithType through dao",
                new Runnable() {
                    @Override
                    public void run() {
                        dao.getBuildingWithTypeByBuildingId(wheatFieldId);
                    }
                }
        );
    }

    @Test
    public void load_BuildingType() {
        trace(
                "Load BuildingType through dao",
                new Runnable() {
                    @Override
                    public void run() {
                        dao.getBuildingType(WHEAT_FIELD_BUILDING_TYPE_ID);
                    }
                }
        );
        start();
    }

    @Test
    public void load_BuildingFromSqlite() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        final RoosterDatabase database = RoosterDatabase.getInstance(targetContext);

        trace(
                "Read Building directly from the SQLLite.",
                new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = database.query(
                                "select * from building where id=?",
                                new Object[]{wheatFieldId});
                        try {
                            if (cursor.moveToFirst()) {
                                long id = cursor.getLong(0);
                                Log.i(LOG_TAG, "load_: Reading cursor; id = " + id);
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                }
        );
    }

    @Test
    public void explain_queryBuilding() {
        explain("explain select * from building where id=?");
    }

    @Test
    public void explain_queryBuildingWithType() {
        explain("explain select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) where building.id=:buildingId");
    }

    @Test
    public void load_BuildingFromRepository() {
        Log.i(LOG_TAG, "load_BuildingFromRepository: dao is " + dao);
        final BuildingTypeRepository repository = BuildingTypeRepository.get(dao);

        trace(
                "Read Building building from repository.",
                new Runnable() {
                    @Override
                    public void run() {
                        repository.queryBuildingWithType(wheatFieldId);
                    }
                }
        );
    }


    @Test
    public void load_BuildingTypeFromRepository() {
        final BuildingTypeRepository repository = BuildingTypeRepository.get(dao);

        trace(
                "Read BuildingType from repository.",
                new Runnable() {
                    @Override
                    public void run() {
                        repository.getBuildingType(WHEAT_FIELD_BUILDING_TYPE_ID);
                    }
                }
        );
    }

    private void start() {
        start = System.currentTimeMillis();
    }

    private long stop(String msg) {
        long end = System.currentTimeMillis();
        Log.i(LOG_TAG, "stop: " + msg + " " + (end - start) + "ms.");
        return end - start;
    }

    private void trace(String msg, Runnable action) {
        long total = 0;
        for (int i = 0; i < REPETITION_COUNT; i++) {
            start();
            action.run();
            total += stop(msg);
        }
        Log.i(LOG_TAG, "trace: Average for " + msg + " "
                + (total / REPETITION_COUNT) + " ms.");
    }

    private void explain(String sql) {
        Context targetContext = InstrumentationRegistry.getTargetContext();

        SqliteUtil.explain(targetContext, sql);
    }
}
