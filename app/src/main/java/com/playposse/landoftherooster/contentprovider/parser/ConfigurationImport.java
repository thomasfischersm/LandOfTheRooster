package com.playposse.landoftherooster.contentprovider.parser;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.playposse.landoftherooster.BuildConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
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

    public static void importAll(Context context) throws IOException {
        // Gather information.
        List<BuildingType> buildingTypes = ConfigurationParser.readBuildingTypes(context);
        int jsonBuildingTypeCount = buildingTypes.size();
        int dbBuildingTypeCount = readDbBuildingTypeCount(context);
        RoosterDatabase db = RoosterDatabase.getInstance(context);

        // Skip if already imported.
        if (jsonBuildingTypeCount == dbBuildingTypeCount) {
            Log.d(LOG_TAG, "importAll: Import has already run previously.");
            return;
        }

        // Reset the types in the database if the configuration files have changed.
        if (dbBuildingTypeCount > 0) {
            Log.d(LOG_TAG, "importAll: Deleting old configuration data from the db.");
            RoosterDao dao = db.getDao();
            dao.deleteBuildingTypes();
        }

        importResourceTypes(context);
        importUnitTypes(context);
        importBuildingTypes(context, buildingTypes);

        if (BuildConfig.DEBUG) {
            DatabaseDumper.dumpTables(db.getOpenHelper());
        }
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
            roomResourceType.setPrecursorResourceTypeId(resourceType.getPrecursorResourceTypeId());
            roomResourceType.setPrecursorUnitTypeId(resourceType.getPrecursorUnitTypeId());

            rows.add(roomResourceType);
        }

        RoosterDatabase.getInstance(context).getDao().insertResourceTypes(rows);
    }

    private static void importUnitTypes(Context context) throws IOException {
        List<UnitType> unitTypes = ConfigurationParser.readUnitTypes(context);

        if (unitTypes == null) {
            Log.w(LOG_TAG, "importUnitTypes: No unit types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.UnitType> rows =
                new ArrayList<>(unitTypes.size());
        for (UnitType unitType : unitTypes) {
            com.playposse.landoftherooster.contentprovider.room.UnitType roomUnitType =
                    new com.playposse.landoftherooster.contentprovider.room.UnitType();

            roomUnitType.setId(unitType.getId());
            roomUnitType.setName(unitType.getName());
            roomUnitType.setCarryingCapacity(unitType.getCarryingCapacity());
            roomUnitType.setAttack(unitType.getAttack());
            roomUnitType.setDefense(unitType.getDefense());
            roomUnitType.setArmor(unitType.getArmor());
            roomUnitType.setHealth(unitType.getHealth());
            roomUnitType.setPrecursorResourceTypeId(unitType.getPrecursorResourceTypeId());
            roomUnitType.setPrecursorUnitTypeId(unitType.getPrecursorUnitTypeId());

            rows.add(roomUnitType);
        }

        RoosterDatabase.getInstance(context).getDao().insertUnitTypes(rows);
    }

    private static void importBuildingTypes(Context context, List<BuildingType> buildingTypes)
            throws IOException {

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
            roomBuildingType.setProducedUnitTypeId(buildingType.getProducedUnitTypeId());
            roomBuildingType.setMinDistanceMeters(buildingType.getMinDistanceMeters());
            roomBuildingType.setMaxDistanceMeters(buildingType.getMaxDistanceMeters());

            rows.add(roomBuildingType);
        }

        RoosterDatabase.getInstance(context).getDao().insertBuildingTypes(rows);
    }

    private static int readDbBuildingTypeCount(Context context) throws IOException {
        Cursor cursor =
                RoosterDatabase.getInstance(context).getDao().getCursorForBuildingTypeCount();

        try {
            return cursor.getCount();
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
