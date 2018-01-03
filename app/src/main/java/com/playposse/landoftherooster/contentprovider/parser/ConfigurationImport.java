package com.playposse.landoftherooster.contentprovider.parser;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.playposse.landoftherooster.BuildConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.services.BuildingDiscoveryService;
import com.playposse.landoftherooster.util.DatabaseDumper;

import java.io.IOException;
import java.util.ArrayList;
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
            DatabaseDumper.dumpTables(RoosterDatabase.getInstance(context).getOpenHelper());
        }

        BuildingDiscoveryService.getNextBuildingTypeadsf(context);
    }

    private static void importResourceTypes(Context context) throws IOException {
        List<ResourceType> resourceTypes = ConfigurationParser.readResourceTypes(context);

        if (resourceTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.ResourceType> rows =
                new ArrayList<>(resourceTypes.size());
        for (ResourceType resourceType : resourceTypes) {
            com.playposse.landoftherooster.contentprovider.room.ResourceType roomResourceType =
                    new com.playposse.landoftherooster.contentprovider.room.ResourceType();

            roomResourceType.setId(resourceType.getId());
            roomResourceType.setName(resourceType.getName());
            roomResourceType.setPrecursorId(resourceType.getPrecursorId());

            rows.add(roomResourceType);
        }

        RoosterDatabase.getInstance(context).getDao().insertResourceTypes(rows);
    }

    private static void importBuildingTypes(Context context) throws IOException {
        List<BuildingType> buildingTypes = ConfigurationParser.readBuildingTypes(context);

        if (buildingTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.BuildingType> rows =
                new ArrayList<>(buildingTypes.size());
        for (BuildingType buildingType : buildingTypes) {
            com.playposse.landoftherooster.contentprovider.room.BuildingType roomBuildingType =
                    new com.playposse.landoftherooster.contentprovider.room.BuildingType();
            roomBuildingType.setId(buildingType.getId());
            roomBuildingType.setName(buildingType.getName());
            roomBuildingType.setIcon(buildingType.getIcon());
            roomBuildingType.setProducedResourceTypeId(buildingType.getProducedResourceTypeId());
            roomBuildingType.setMinDistanceMeters(buildingType.getMinDistanceMeters());
            roomBuildingType.setMaxDistanceMeters(buildingType.getMaxDistanceMeters());

            rows.add(roomBuildingType);
        }

        RoosterDatabase.getInstance(context).getDao().insertBuildingTypes(rows);
    }

    private static boolean hasAlreadyImported(Context context) {
        Cursor cursor =
                RoosterDatabase.getInstance(context).getDao().getCursorForBuildingTypeCount();

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