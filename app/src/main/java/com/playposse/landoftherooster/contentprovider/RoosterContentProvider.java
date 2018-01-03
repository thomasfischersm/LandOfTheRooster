package com.playposse.landoftherooster.contentprovider;

import android.content.ContentProvider;
import android.database.sqlite.SQLiteOpenHelper;

import com.playposse.landoftherooster.util.BasicContentProvider;

/**
 * A {@link ContentProvider} that stores the rule sets.
 */
public class RoosterContentProvider extends BasicContentProvider {

    private static final int RESOURCE_TYPE_TABLE_KEY = 1;
    private static final int RESOURCE_TABLE_KEY = 2;
    private static final int BUILDING_TYPE_TABLE_KEY = 3;
    private static final int BUILDING_TABLE_KEY = 4;

    public RoosterContentProvider() {
//        addTable(
//                RESOURCE_TYPE_TABLE_KEY,
//                RoosterContentContract.AUTHORITY,
//                ResourceTypeTable.PATH,
//                ResourceTypeTable.CONTENT_URI,
//                ResourceTypeTable.TABLE_NAME);
//
//        addTable(
//                RESOURCE_TABLE_KEY,
//                RoosterContentContract.AUTHORITY,
//                ResourceTable.PATH,
//                ResourceTable.CONTENT_URI,
//                ResourceTable.TABLE_NAME);
//
//        addTable(
//                BUILDING_TYPE_TABLE_KEY,
//                RoosterContentContract.AUTHORITY,
//                BuildingTypeTable.PATH,
//                BuildingTypeTable.CONTENT_URI,
//                BuildingTypeTable.TABLE_NAME);
//
//        addTable(
//                BUILDING_TABLE_KEY,
//                RoosterContentContract.AUTHORITY,
//                BuildingTable.PATH,
//                BuildingTable.CONTENT_URI,
//                BuildingTable.TABLE_NAME);
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper() {
        return new RoosterDatabaseHelper(getContext());
    }
}
