package com.playposse.landoftherooster;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.playposse.landoftherooster.analytics.Analytics;
import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.services.GameBackgroundService;

import io.fabric.sdk.android.Fabric;

import static com.playposse.landoftherooster.activity.KingdomActivity.PRODUCT_FLAVOR_DEV_MODE_ON;

/**
 * Implementation of {@link Application}.
 */
public class RoosterApplication extends MultiDexApplication {

    private static final String LOG_TAG = RoosterApplication.class.getSimpleName();

    private static boolean debugDataComplete = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        GameConfig.init(this);

        Analytics.init(this);

        // Start with a fresh database when running for debug.
        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals(PRODUCT_FLAVOR_DEV_MODE_ON)) {
            getApplicationContext().deleteDatabase(RoosterDatabaseHelper.DB_NAME);
            Log.d(LOG_TAG, "onCreate: Reset RoosterDatabase");
        }

        // Import configuration on the first run.
//        ConfigurationImport.startImport(this);

//        new CreateDebugDataAsyncTask().execute();
        debugDataComplete = true;

        startGameBackgroundService(this);
    }

    private void createUnits(int amount, long unitTypeId) {
        RoosterDao dao = RoosterDatabase.getInstance(this).getDao();
        UnitType unitType = dao.getUnitTypeById(unitTypeId);

        for (int i = 0; i < amount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitTypeId);
            unit.setHealth(unitType.getHealth());
            dao.insert(unit);
        }
    }

    public static void startGameBackgroundService(Context context) {
        context.startService(new Intent(context, GameBackgroundService.class));
    }

    public static boolean isDebugDataComplete() {
        return debugDataComplete;
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
            createUnits(5, 1);

            debugDataComplete = true;
            Log.d(LOG_TAG, "doInBackground: Done creating debug test data.");
            return null;
        }
    }
}
