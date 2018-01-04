package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;

/**
 * A Room database that goes to our Sqlite instance.
 */
@Database(entities = {Building.class, BuildingType.class, ResourceType.class},
        version = 3,
        exportSchema = true)
public abstract class RoosterDatabase extends RoomDatabase {

    private static RoosterDatabase instance;

    public abstract RoosterDao getDao();

    public static RoosterDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    RoosterDatabase.class,
                    RoosterDatabaseHelper.DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
