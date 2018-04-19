package com.playposse.landoftherooster;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.services.LocationScanningService;

/**
 * Implementation of {@link Application}.
 */
public class RoosterApplication extends Application {

    private static final String LOG_TAG = RoosterApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        GameConfig.init(this);

        // Start with a fresh database when running for debug.
        if (BuildConfig.DEBUG) {
            getApplicationContext().deleteDatabase(RoosterDatabaseHelper.DB_NAME);
            Log.i(LOG_TAG, "onCreate: Reset RoosterDatabase");
        }

        // Import configuration on the first run.
//        ConfigurationImport.startImport(this);

        new CreateDebugDataAsyncTask().execute();

        startService(new Intent(this, LocationScanningService.class));
    }

    private void createUnits(int amount, int unitTypeId) {
        RoosterDao dao = RoosterDatabase.getInstance(this).getDao();
        UnitType unitType = dao.getUnitTypeById(unitTypeId);

        for (int i = 0; i < amount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitTypeId);
            unit.setHealth(unitType.getHealth());
            dao.insert(unit);
        }
    }

    /**
     * An {@link AsyncTask} that creates data in the database for debugging.
     */
    class CreateDebugDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            RoosterDao dao = RoosterDatabase.getInstance(RoosterApplication.this).getDao();
            dao.deleteUnits();

            createUnits(6, 2);
            createUnits(1, 1);
            return null;
        }
    }
}
