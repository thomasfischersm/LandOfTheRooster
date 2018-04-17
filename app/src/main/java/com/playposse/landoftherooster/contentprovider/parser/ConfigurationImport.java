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

//    public static void startImport(Context context) {
//        new ImportAsyncTask(context).execute();
//    }

    public static void importAll(Context context, RoosterDatabase database) throws IOException {
        RoosterDao dao = database.getDao();

        // Gather information.
        List<BuildingType> buildingTypes = ConfigurationParser.readBuildingTypes(context);
        int jsonBuildingTypeCount = buildingTypes.size();
        int dbBuildingTypeCount = readDbBuildingTypeCount(dao);

        // Skip if already imported.
        if (jsonBuildingTypeCount == dbBuildingTypeCount) {
            Log.d(LOG_TAG, "importAll: Import has already run previously.");
            return;
        }

        // Reset the types in the database if the configuration files have changed.
        if (dbBuildingTypeCount > 0) {
            Log.d(LOG_TAG, "importAll: Deleting old configuration data from the db.");
            dao.deleteBuildingTypes();
        }

        importResourceTypes(context, dao);
        importUnitTypes(context, dao);
        importBuildingTypes(dao, buildingTypes);
        importProductionRules(context, dao);

        if (BuildConfig.DEBUG) {
            DatabaseDumper.dumpTables(database.getOpenHelper());
        }
    }

    private static void importResourceTypes(Context context, RoosterDao dao) throws IOException {
        List<ResourceType> resourceTypes = ConfigurationParser.readResourceTypes(context);

        if (resourceTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.entity.ResourceType> rows =
                new ArrayList<>(resourceTypes.size());
        for (ResourceType resourceType : resourceTypes) {
            com.playposse.landoftherooster.contentprovider.room.entity.ResourceType roomResourceType =
                    new com.playposse.landoftherooster.contentprovider.room.entity.ResourceType();

            roomResourceType.setId(resourceType.getId());
            roomResourceType.setName(resourceType.getName());

            rows.add(roomResourceType);
        }

        dao.insertResourceTypes(rows);
    }

    private static void importUnitTypes(Context context, RoosterDao dao) throws IOException {
        List<UnitType> unitTypes = ConfigurationParser.readUnitTypes(context);

        if (unitTypes == null) {
            Log.w(LOG_TAG, "importUnitTypes: No unit types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.entity.UnitType> rows =
                new ArrayList<>(unitTypes.size());
        for (UnitType unitType : unitTypes) {
            com.playposse.landoftherooster.contentprovider.room.entity.UnitType roomUnitType =
                    new com.playposse.landoftherooster.contentprovider.room.entity.UnitType();

            roomUnitType.setId(unitType.getId());
            roomUnitType.setName(unitType.getName());
            roomUnitType.setCarryingCapacity(unitType.getCarryingCapacity());
            roomUnitType.setAttack(unitType.getAttack());
            roomUnitType.setDefense(unitType.getDefense());
            roomUnitType.setDamage(unitType.getDamage());
            roomUnitType.setArmor(unitType.getArmor());
            roomUnitType.setHealth(unitType.getHealth());

            rows.add(roomUnitType);
        }

        dao.insertUnitTypes(rows);
    }

    private static void importBuildingTypes(RoosterDao dao, List<BuildingType> buildingTypes)
            throws IOException {

        if (buildingTypes == null) {
            Log.w(LOG_TAG, "importResourceTypes: No resource types found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.entity.BuildingType> rows =
                new ArrayList<>(buildingTypes.size());
        for (BuildingType buildingType : buildingTypes) {
            com.playposse.landoftherooster.contentprovider.room.entity.BuildingType roomBuildingType =
                    new com.playposse.landoftherooster.contentprovider.room.entity.BuildingType();
            roomBuildingType.setId(buildingType.getId());
            roomBuildingType.setName(buildingType.getName());
            roomBuildingType.setIcon(buildingType.getIcon());
            roomBuildingType.setMinDistanceMeters(buildingType.getMinDistanceMeters());
            roomBuildingType.setMaxDistanceMeters(buildingType.getMaxDistanceMeters());
            roomBuildingType.setEnemyUnitCount(buildingType.getEnemyUnitCount());
            roomBuildingType.setEnemyUnitTypeId(buildingType.getEnemyUnitTypeId());
            roomBuildingType.setConquestPrizeResourceTypeId(buildingType.getConquestPrizeResourceTypeId());

            rows.add(roomBuildingType);
        }

        dao.insertBuildingTypes(rows);
    }

    private static void importProductionRules(Context context, RoosterDao dao) throws IOException {
        List<ProductionRule> productionRules = ConfigurationParser.readProductionRules(context);

        if (productionRules == null) {
            Log.w(LOG_TAG, "importProductionRules: No production rules found!");
            return;
        }

        List<com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule> rows =
                new ArrayList<>();
        for (ProductionRule productionRule : productionRules) {
            com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule roomProductionRule =
                    new com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule();

            roomProductionRule.setId(productionRule.getId());
            roomProductionRule.setBuildingId(productionRule.getBuildingId());
            roomProductionRule.setInputResourceTypeIds(productionRule.getInputResourceTypeIds());
            roomProductionRule.setInputUnitTypeIds(productionRule.getInputUnitTypeIds());
            roomProductionRule.setOutputResourceTypeId(productionRule.getOutputResourceTypeId());
            roomProductionRule.setOutputUnitTypeId(productionRule.getOutputUnitTypeId());

            rows.add(roomProductionRule);
        }

        dao.insertProductionRules(rows);
    }

    private static int readDbBuildingTypeCount(RoosterDao dao) throws IOException {
        Cursor cursor = dao.getCursorForBuildingTypeCount();

        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }

    /**
     * An {@link AsyncTask} that imports the configuration.
     */
//    private static final class ImportAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        private final Context context;
//
//        private ImportAsyncTask(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                importAll(context, RoosterDatabase.getInstance(context));
//            } catch (IOException ex) {
//                Log.e(LOG_TAG, "doInBackground: Failed to import configuration data.", ex);
//            }
//            return null;
//        }
//    }
}
