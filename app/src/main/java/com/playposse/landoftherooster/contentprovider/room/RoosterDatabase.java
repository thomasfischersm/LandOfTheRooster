package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.contentprovider.parser.ConfigurationImport;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.util.MutableBoolean;

import java.io.IOException;

/**
 * A Room database that goes to our Sqlite instance.
 */
@Database(entities = {
        Building.class,
        BuildingType.class,
        ProductionRule.class,
        Resource.class,
        ResourceType.class,
        Unit.class,
        UnitType.class},
        version = 13,
        exportSchema = true)
@TypeConverters({DateConverter.class})
public abstract class RoosterDatabase extends RoomDatabase {

    private static final String LOG_TAG = RoosterDatabase.class.getSimpleName();

    private static RoosterDatabase instance;

    public abstract RoosterDao getDao();

    public static synchronized RoosterDatabase getInstance(Context context) {
        Log.i(LOG_TAG, "getInstance: RoosterDatabase getInstance is called.");
        if (instance == null) {
            MutableBoolean isDbInitNeeded = new MutableBoolean(false);
            instance = Room.databaseBuilder(
                    context,
                    RoosterDatabase.class,
                    RoosterDatabaseHelper.DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoosterDatabaseCallback(context, isDbInitNeeded))
                    .build();

            // Force the database to be initialized by making a simple call.
            instance.getDao().getLastBuilding();

            Log.i(LOG_TAG, "getInstance: isDbInitNeeded = " + isDbInitNeeded.isValue());
            if (isDbInitNeeded.isValue()) {
                try {
                    ConfigurationImport.importAll(context, instance);
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "getInstance: Failed to import initial data.", ex);
                }
            }
        }
        Log.i(LOG_TAG, "getInstance: RoosterDatabase getInstance has completed.");
        return instance;
    }

    /**
     * {@link Callback} that loads the initial data into the the database on creation
     */
    private static class RoosterDatabaseCallback extends Callback {

        private final Context context;
        private final MutableBoolean isDbInitNeeded;

        private RoosterDatabaseCallback(Context context, MutableBoolean isDbInitNeeded) {
            this.context = context;
            this.isDbInitNeeded = isDbInitNeeded;
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            Log.e(LOG_TAG, "onCreate: Importing configuration into new db.");
//            ConfigurationImport.startImport(context);
            isDbInitNeeded.setValue(true);
        }
    }
}
