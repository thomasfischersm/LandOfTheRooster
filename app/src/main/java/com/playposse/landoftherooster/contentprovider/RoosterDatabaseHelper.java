package com.playposse.landoftherooster.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.playposse.landoftherooster.contentprovider.RoosterContentContract.BuildingTable;
import com.playposse.landoftherooster.contentprovider.RoosterContentContract.BuildingTypeTable;
import com.playposse.landoftherooster.contentprovider.RoosterContentContract.ResourceTypeTable;
import com.playposse.landoftherooster.contentprovider.RoosterContentContract.ResourceTable;

/**
 * A helper class that manages the SQLLite database.
 */
public class RoosterDatabaseHelper extends SQLiteOpenHelper {

    public  static final String DB_NAME = "rooster";

    private static final int DB_VERSION = 1;

    public RoosterDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ResourceTypeTable.SQL_CREATE_TABLE);
        db.execSQL(ResourceTable.SQL_CREATE_TABLE);
        db.execSQL(BuildingTypeTable.SQL_CREATE_TABLE);
        db.execSQL(BuildingTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to upgrade. This is the first version.
    }
}
