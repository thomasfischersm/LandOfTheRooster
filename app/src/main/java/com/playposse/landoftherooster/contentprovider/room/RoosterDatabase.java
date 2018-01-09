package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.contentprovider.parser.ConfigurationImport;

/**
 * A Room database that goes to our Sqlite instance.
 */
@Database(entities = {Building.class, BuildingType.class, Resource.class, ResourceType.class},
        version = 5,
        exportSchema = true)
public abstract class RoosterDatabase extends RoomDatabase {

    private static final String LOG_TAG = RoosterDatabase.class.getSimpleName();

    private static RoosterDatabase instance;

    public abstract RoosterDao getDao();

    public static RoosterDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    RoosterDatabase.class,
                    RoosterDatabaseHelper.DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoosterDatabaseCallback(context))
                    .build();
        }
        return instance;
    }

    /**
     * {@link Callback} that loads the initial data into the the database on creation
     */
    private static class RoosterDatabaseCallback extends Callback {

        private final Context context;

        private RoosterDatabaseCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            Log.e(LOG_TAG, "onCreate: Importing configuration into new db.");
            ConfigurationImport.startImport(context);
        }
    }
}
