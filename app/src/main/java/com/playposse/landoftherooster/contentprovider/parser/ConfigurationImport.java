package com.playposse.landoftherooster.contentprovider.parser;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.playposse.landoftherooster.BuildConfig;
import com.playposse.landoftherooster.contentprovider.RoosterContentContract.BuildingTypeTable;
import com.playposse.landoftherooster.contentprovider.RoosterContentContract.ResourceTypeTable;
import com.playposse.landoftherooster.contentprovider.RoosterDatabaseHelper;
import com.playposse.landoftherooster.util.DatabaseDumper;

import java.io.IOException;
import java.util.List;

/**
 * An importer for configuration data from JSON files to the Sqlite database.
 */
public final class ConfigurationImport {
    private static final String LOG_TAG = ConfigurationImport.class.getSimpleName();

    private ConfigurationImport() {
    }

    public static void startImport(Context context) {
        new ImportAsyncTask(context).execute();
    }

    private static void importAll(Context context) throws IOException {
        if (hasAlreadyImported(context)) {
            Log.d(LOG_TAG, "importAll: Import has already run previously.");
            return;
        }
        
        importResourceTypes(context);
        importBuildingTypes(context);

        if (BuildConfig.DEBUG) {
            DatabaseDumper.dumpTables(new RoosterDatabaseHelper(context));
        }
    }

    private static void importResourceTypes(Context context) throws IOException {
        List<ResourceType> resourceTypes = ConfigurationParser.readResourceTypes(context);

        if (resourceTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        ContentValues[] rows = new ContentValues[resourceTypes.size()];
        for (int i = 0; i < resourceTypes.size(); i++) {
            ResourceType resourceType = resourceTypes.get(i);

            ContentValues values = new ContentValues();
            values.put(ResourceTypeTable.ID_COLUMN, resourceType.getId());
            values.put(ResourceTypeTable.NAME_COLUMN, resourceType.getName());
            values.put(ResourceTypeTable.PRECURSOR_ID_COLUMN, resourceType.getPrecursorId());

            rows[i] = values;
        }

        context.getContentResolver().bulkInsert(ResourceTypeTable.CONTENT_URI, rows);
    }

    private static void importBuildingTypes(Context context) throws IOException {
        List<BuildingType> buildingTypes = ConfigurationParser.readBuildingTypes(context);

        if (buildingTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        ContentValues[] rows = new ContentValues[buildingTypes.size()];
        for (int i = 0; i < buildingTypes.size(); i++) {
            BuildingType buildingType = buildingTypes.get(i);

            ContentValues values = new ContentValues();
            values.put(BuildingTypeTable.ID_COLUMN, buildingType.getId());
            values.put(BuildingTypeTable.NAME_COLUMN, buildingType.getName());
            values.put(BuildingTypeTable.ICON_COLUMN, buildingType.getIcon());
            values.put(
                    BuildingTypeTable.PRODUCED_RESOURCE_TYPE_ID_COLUMN,
                    buildingType.getProducedResourceTypeId());
            values.put(BuildingTypeTable.MIN_DISTANCE_METERS_COLUMN, buildingType.getMinDistanceMeters());
            values.put(BuildingTypeTable.MAX_DISTANCE_METERS_COLUMN, buildingType.getMaxDistanceMeters());

            rows[i] = values;
        }

        context.getContentResolver().bulkInsert(BuildingTypeTable.CONTENT_URI, rows);
    }

    private static boolean hasAlreadyImported(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            throw new IllegalStateException("ContentResolver is null!");
        }

        Cursor cursor = contentResolver.query(
                ResourceTypeTable.CONTENT_URI,
                ResourceTypeTable.COLUMN_NAMES,
                null,
                null,
                null);

        if (cursor == null) {
            throw new IllegalStateException("Got a null cursor");
        }

        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * An {@link AsyncTask} that imports the configuration.
     */
    private static final class ImportAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Context context;

        private ImportAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                importAll(context);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "doInBackground: Failed to import configuration data.", ex);
            }
            return null;
        }
    }
}
