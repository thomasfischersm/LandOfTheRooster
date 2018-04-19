package com.playposse.landoftherooster.services;

import android.content.Context;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that executes the production of a building.
 */
final class ProductionExecutor {

    private static final String LOG_TAG = ProductionExecutor.class.getSimpleName();

    private ProductionExecutor() {}

    /**
     * new production method after production rules are introduced.
     */
    static void produce(Context context, BuildingWithType buildingWithType) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        int buildingTypeId = buildingWithType.getBuildingType().getId();

        List<ProductionRule> productionRules =
                dao.getProductionRulesByBuildingTypeId(buildingTypeId);

        if ((productionRules == null) || (productionRules.size() ==0)) {
            Log.i(LOG_TAG, "produce2: There are no production rules for building type "
                    + buildingTypeId);
            return;
        }

        // Produce as many units as possible.
        for (ProductionRule productionRule : productionRules) {
            while (produceOneThing(context, dao, productionRule));
        }
    }

    private static boolean produceOneThing(
            Context context,
            RoosterDao dao,
            ProductionRule productionRule) {

        // Check carrying capacity.
        Integer outputResourceTypeId = productionRule.getOutputResourceTypeId();
        boolean producesResource = (outputResourceTypeId != null);
        boolean consumesResource = !StringUtil.isEmpty(productionRule.getInputResourceTypeIds());
        if (producesResource && !consumesResource) {
            // The production rule may convert a unit to a resource or produce a resource for free.
            int maxCarryingCapacity = dao.getCarryingCapacity() + 1; // +1 for the user itself.
            int freeCarryingCapacity = maxCarryingCapacity - dao.getResourceCountJoiningUser();

            if (freeCarryingCapacity < 1) {
                Log.i(LOG_TAG, "produceOneThing: The user has no more carrying capacity left.");
                return false;
            }
        }

        // Check for required precursors.
        Map<Integer, Resource> resourceMap = new HashMap<>();
        List<Integer> inputResourceTypeIds =
                StringUtil.splitToIntList(productionRule.getInputResourceTypeIds());
        for (int resourceTypeId : inputResourceTypeIds) {
            Resource resource = dao.getResourceJoiningUserByTypeId(resourceTypeId);
            if ((resource == null) || (resource.getAmount() < 1)) {
                Log.d(LOG_TAG, "produceOneThing: Missing resource type " + resourceTypeId);
                return false;
            }

            resourceMap.put(resourceTypeId, resource);
        }

        // Check for required precursors.
        Map<Integer, Unit> unitMap = new HashMap<>();
        List<Integer> inputUnitTypeIds =
                StringUtil.splitToIntList(productionRule.getInputUnitTypeIds());
        for (int unitTypeId : inputUnitTypeIds) {
            List<Unit> units = dao.getUnitsJoiningUserByTypeId(unitTypeId);
            if ((units == null) || (units.size() < 1)) {
                Log.d(LOG_TAG, "produceOneThing: Missing unit type " + unitTypeId);
                return false;
            }

            unitMap.put(unitTypeId, units.get(0));
        }

        // Debit resources.
        for (int resourceTypeId : inputResourceTypeIds) {
            Resource resource = resourceMap.get(resourceTypeId);
            RoosterDaoUtil.creditResource(
                    context,
                    resource,
                    resourceTypeId,
                    -1,
                    null);
        }

        // Debit units.
        for (Unit unit : unitMap.values()) {
            dao.deleteUnit(unit);
        }

        // Credit production
        if (outputResourceTypeId != null) {
            RoosterDaoUtil.creditResource(context, outputResourceTypeId, 1, null);
        } else {
            Integer outputUnitTypeId = productionRule.getOutputUnitTypeId();
            if (outputUnitTypeId != null) {
                UnitType unitType = dao.getUnitTypeById(outputUnitTypeId);
                RoosterDaoUtil.creditUnit(context, unitType, 1, null);
            }
        }

        return true;
    }
//
//    /**
//     * Old production method before production rules.
//     */
//    static void produce(Context context, BuildingWithType buildingWithType) {
//        Log.d(LOG_TAG, "produce: Attempting production rules for building "
//                + buildingWithType.getBuildingType().getName());
//
//        // Find out what is produced.
//        ResourceType producedResourceType = getProducedResourceType(context, buildingWithType);
//        UnitType producedUnitType = getProducedUnitType(context, buildingWithType);
//
//        // Exit early if the building doesn't produce anything.
//        if ((producedResourceType == null) && (producedUnitType == null)) {
//            // Nothing is produced here.
//            Log.d(LOG_TAG, "produce: Building doesn't produce anything: "
//                    + buildingWithType.getBuildingType().getName());
//            return;
//        }
//
//        // Find out about the precursor.
//        final ResourceType precursorResourceType;
//        final UnitType precursorUnitType;
//        if (producedResourceType != null) {
//            precursorResourceType = getPrecursorResourceType(context, producedResourceType);
//            precursorUnitType = getPrecursorUnitType(context, producedResourceType);
//        } else if (producedUnitType != null) {
//            precursorResourceType = getPrecursorResourceType(context, producedUnitType);
//            precursorUnitType = getPrecursorUnitType(context, producedUnitType);
//        } else {
//            Log.d(LOG_TAG, "produce: Unexpected case");
//            return;
//        }
//
//        // Check if the user has a free storage slot
//        int productionAmount = calculateProductionAmount(
//                context,
//                precursorResourceType,
//                precursorUnitType,
//                producedResourceType,
//                producedUnitType);
//        if (productionAmount <= 0) {
//            Log.d(LOG_TAG, "produce: Cannot produce because the user doesn't have spare " +
//                    "carrying capacity.");
//            // TODO: Notify the user with a vibration and message.
//            return;
//        }
//
//        // Debit precursor.
//        if ((precursorResourceType == null) && (precursorUnitType == null)) {
//            Log.d(LOG_TAG, "produce: Building doesn't require resource precursor: "
//                    + buildingWithType.getBuildingType().getName());
//        } else if (precursorResourceType != null) {
//            // Try to debit precursor resource type
//
//            if (!debit(context, precursorResourceType, productionAmount)) {
//                Log.d(LOG_TAG, "produce: Building requires unit precursor: "
//                        + buildingWithType.getBuildingType().getName()
//                        + " that the user is missing: " + precursorResourceType.getName());
//                return;
//            }
//        } else if (precursorUnitType != null) {
//            // Try to debit precursor resource type
//
//            if (!debit(context, precursorUnitType, productionAmount)) {
//                Log.d(LOG_TAG, "produce: Building requires precursor: "
//                        + buildingWithType.getBuildingType().getName()
//                        + " that the user is missing: " + precursorUnitType.getName());
//                return;
//            }
//        }
//
//        // Credit production.
//        if (producedResourceType != null) {
//            RoosterDaoUtil.creditUnit(context, producedResourceType, productionAmount);
//        } else if (producedUnitType != null) {
//            RoosterDaoUtil.creditUnit(context, producedUnitType, productionAmount);
//        }
//    }
//
//    private static int calculateProductionAmount(
//            Context context,
//            @Nullable ResourceType precursorResourceType,
//            @Nullable UnitType precursorUnitType,
//            @Nullable ResourceType producedResourceType,
//            @Nullable UnitType producedUnitType) {
//
//        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//        int precursorResourceAmount = RoosterDaoUtil.getResourceAmount(context, precursorResourceType);
//        int precursorUnitAmount = RoosterDaoUtil.getUnitAmount(context, precursorUnitType);
//        int precursorAmount = Math.max(precursorResourceAmount, precursorUnitAmount);
//        boolean isPrecursorRequired =
//                (precursorResourceType != null) || (precursorUnitType != null);
//
//        if (producedUnitType != null) {
//            // production doesn't require carrying capacity. Only the precursor amount is limiting.
//            return (precursorResourceType != null) ? precursorResourceAmount : precursorUnitAmount;
//        }
//
//        int maxCarryingCapacity = dao.getCarryingCapacity() + 1; // +1 for the user itself.
//        int freeCarryingCapacity = maxCarryingCapacity - dao.getResourceCountJoiningUser();
//        int futureAvailableCarryingCapacity =
//                Math.min(maxCarryingCapacity, freeCarryingCapacity + precursorResourceAmount);
//        int productionAmount =
//                isPrecursorRequired
//                        ? Math.min(precursorAmount, futureAvailableCarryingCapacity)
//                        : futureAvailableCarryingCapacity;
//
//        Log.i(LOG_TAG, "calculateProductionAmount: Determined production amount: "
//                + productionAmount);
//
//        return productionAmount;
//    }
//
//    @Nullable
//    private static ResourceType getProducedResourceType(
//            Context context,
//            BuildingWithType buildingWithType) {
//
//        Integer producedResourceTypeId =
//                buildingWithType.getBuildingType().getProducedResourceTypeId();
//
//        if (producedResourceTypeId == null) {
//            // Building doesn't produce anything.
//            return null;
//        }
//
//        // Query the database.
//        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//        return dao.getResourceTypeById(producedResourceTypeId);
//    }
//
//    @Nullable
//    private static UnitType getProducedUnitType(
//            Context context,
//            BuildingWithType buildingWithType) {
//
//        Integer producedUnitTypeId =
//                buildingWithType.getBuildingType().getProducedUnitTypeId();
//
//        if (producedUnitTypeId == null) {
//            // Building doesn't produce anything.
//            return null;
//        }
//
//        // Query the database.
//        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//        return dao.getUnitTypeById(producedUnitTypeId);
//    }
//
//    @Nullable
//    private static ResourceType getPrecursorResourceType(
//            Context context,
//            ResourceType producedResourceType) {
//
//        if ((producedResourceType == null)) {
//            return null;
//        }
//
//        Integer resourceTypeId = producedResourceType.getPrecursorResourceTypeId();
//        if (resourceTypeId != null) {
//            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//            return dao.getResourceTypeById(resourceTypeId);
//        } else {
//            return null;
//        }
//    }
//
//
//    @Nullable
//    private static ResourceType getPrecursorResourceType(
//            Context context,
//            UnitType producedUnitType) {
//
//        if ((producedUnitType == null)) {
//            return null;
//        }
//
//        Integer resourceTypeId = producedUnitType.getPrecursorResourceTypeId();
//        if (resourceTypeId != null) {
//            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//            return dao.getResourceTypeById(resourceTypeId);
//        } else {
//            return null;
//        }
//    }
//
//    @Nullable
//    private static UnitType getPrecursorUnitType(
//            Context context,
//            ResourceType producedResourceType) {
//
//        if ((producedResourceType == null)) {
//            return null;
//        }
//
//        Integer unitTypeId = producedResourceType.getPrecursorUnitTypeId();
//        if (unitTypeId != null) {
//            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//            return dao.getUnitTypeById(unitTypeId);
//        } else {
//            return null;
//        }
//    }
//
//    @Nullable
//    private static UnitType getPrecursorUnitType(
//            Context context,
//            UnitType producedUnitType) {
//
//        if ((producedUnitType == null)) {
//            return null;
//        }
//
//        Integer unitTypeId = producedUnitType.getPrecursorUnitTypeId();
//        if (unitTypeId != null) {
//            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//            return dao.getUnitTypeById(unitTypeId);
//        } else {
//            return null;
//        }
//    }
//
//    private static boolean debit(Context context, ResourceType resourceType, int productionAmount) {
//        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//        Resource resource = dao.getResourceJoiningUserByTypeId(resourceType.getId());
//
//        if (resource.getAmount() >= productionAmount) {
//            resource.setAmount(resource.getAmount() - productionAmount);
//            dao.update(resource);
//            return true;
//        } else {
//            Log.d(LOG_TAG, "debitResourceType: Building requires precursor: "
//                    + " that the user is missing: " + resourceType.getName());
//            return false;
//        }
//    }
//
//
//    private static boolean debit(Context context, UnitType unitType, int productionAmount) {
//        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
//        List<Unit> units = dao.getUnitsJoiningUserByTypeId(unitType.getId());
//
//        if ((units != null) && (units.size() >= productionAmount)) {
//            for (int i = 0; i < productionAmount; i++) {
//                dao.deleteUnit(units.get(0));
//            }
//            return true;
//        } else {
//            Log.d(LOG_TAG, "debitResourceType: Building requires precursor: "
//                    + " that the user is missing: " + unitType.getName());
//            return false;
//        }
//    }
}
