package com.playposse.landoftherooster.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.Resource;
import com.playposse.landoftherooster.contentprovider.room.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.Unit;
import com.playposse.landoftherooster.contentprovider.room.UnitType;

import java.util.List;

/**
 * A class that executes the production of a building.
 */
class ProductionExecutor {

    private static final String LOG_TAG = ProductionExecutor.class.getSimpleName();

    static void produce(Context context, BuildingWithType buildingWithType) {
        Log.d(LOG_TAG, "produce: Attempting production rules for building "
                + buildingWithType.getBuildingType().getName());

        // Find out what is produced.
        ResourceType producedResourceType = getProducedResourceType(context, buildingWithType);
        UnitType producedUnitType = getProducedUnitType(context, buildingWithType);

        // Exit early if the building doesn't produce anything.
        if ((producedResourceType == null) && (producedUnitType == null)) {
            // Nothing is produced here.
            Log.d(LOG_TAG, "produce: Building doesn't produce anything: "
                    + buildingWithType.getBuildingType().getName());
            return;
        }

        // Find out about the precursor.
        final ResourceType precursorResourceType;
        final UnitType precursorUnitType;
        if (producedResourceType != null) {
            precursorResourceType = getPrecursorResourceType(context, producedResourceType);
            precursorUnitType = getPrecursorUnitType(context, producedResourceType);
        } else if (producedUnitType != null) {
            precursorResourceType = getPrecursorResourceType(context, producedUnitType);
            precursorUnitType = getPrecursorUnitType(context, producedUnitType);
        } else {
            Log.d(LOG_TAG, "produce: Unexpected case");
            return;
        }

        // Check if the user has a free storage slot
        int productionAmount = calculateProductionAmount(
                context,
                precursorResourceType,
                precursorUnitType,
                producedResourceType,
                producedUnitType);
        if (productionAmount <= 0) {
            Log.d(LOG_TAG, "produce: Cannot produce because the user doesn't have spare " +
                    "carrying capacity.");
            // TODO: Notify the user with a vibration and message.
            return;
        }

        // Debit precursor.
        if ((precursorResourceType == null) && (precursorUnitType == null)) {
            Log.d(LOG_TAG, "produce: Building doesn't require resource precursor: "
                    + buildingWithType.getBuildingType().getName());
        } else if (precursorResourceType != null) {
            // Try to debit precursor resource type

            if (!debit(context, precursorResourceType, productionAmount)) {
                Log.d(LOG_TAG, "produce: Building requires unit precursor: "
                        + buildingWithType.getBuildingType().getName()
                        + " that the user is missing: " + precursorResourceType.getName());
                return;
            }
        } else if (precursorUnitType != null) {
            // Try to debit precursor resource type

            if (!debit(context, precursorUnitType, productionAmount)) {
                Log.d(LOG_TAG, "produce: Building requires precursor: "
                        + buildingWithType.getBuildingType().getName()
                        + " that the user is missing: " + precursorUnitType.getName());
                return;
            }
        }

        // Credit production.
        if (producedResourceType != null) {
            credit(context, producedResourceType, productionAmount);
        } else if (producedUnitType != null) {
            credit(context, producedUnitType, productionAmount);
        }
    }

    private static int calculateProductionAmount(
            Context context,
            @Nullable ResourceType precursorResourceType,
            @Nullable UnitType precursorUnitType,
            @Nullable ResourceType producedResourceType,
            @Nullable UnitType producedUnitType) {

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        int precursorResourceAmount = getResourceAmount(context, precursorResourceType);
        int precursorUnitAmount = getUnitAmount(context, precursorUnitType);
        int precursorAmount = Math.max(precursorResourceAmount, precursorUnitAmount);
        boolean isPrecursorRequired =
                (precursorResourceType != null) || (precursorUnitType != null);

        if (producedUnitType != null) {
            // production doesn't require carrying capacity. Only the precursor amount is limiting.
            return (precursorResourceType != null) ? precursorResourceAmount : precursorUnitAmount;
        }

        int maxCarryingCapacity = dao.getCarryingCapacity() + 1; // +1 for the user itself.
        int freeCarryingCapacity = maxCarryingCapacity - dao.getResourceCount();
        int futureAvailableCarryingCapacity =
                Math.min(maxCarryingCapacity, freeCarryingCapacity + precursorResourceAmount);
        int productionAmount =
                isPrecursorRequired
                        ? Math.min(precursorAmount, futureAvailableCarryingCapacity)
                        : futureAvailableCarryingCapacity;

        Log.i(LOG_TAG, "calculateProductionAmount: Determined production amount: "
                + productionAmount);

        return productionAmount;
    }

    @Nullable
    private static ResourceType getProducedResourceType(
            Context context,
            BuildingWithType buildingWithType) {

        Integer producedResourceTypeId =
                buildingWithType.getBuildingType().getProducedResourceTypeId();

        if (producedResourceTypeId == null) {
            // Building doesn't produce anything.
            return null;
        }

        // Query the database.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getResourceTypeById(producedResourceTypeId);
    }

    @Nullable
    private static UnitType getProducedUnitType(
            Context context,
            BuildingWithType buildingWithType) {

        Integer producedUnitTypeId =
                buildingWithType.getBuildingType().getProducedUnitTypeId();

        if (producedUnitTypeId == null) {
            // Building doesn't produce anything.
            return null;
        }

        // Query the database.
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getUnitTypeById(producedUnitTypeId);
    }

    @Nullable
    private static ResourceType getPrecursorResourceType(
            Context context,
            ResourceType producedResourceType) {

        if ((producedResourceType == null)) {
            return null;
        }

        Integer resourceTypeId = producedResourceType.getPrecursorResourceTypeId();
        if (resourceTypeId != null) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            return dao.getResourceTypeById(resourceTypeId);
        } else {
            return null;
        }
    }


    @Nullable
    private static ResourceType getPrecursorResourceType(
            Context context,
            UnitType producedUnitType) {

        if ((producedUnitType == null)) {
            return null;
        }

        Integer resourceTypeId = producedUnitType.getPrecursorResourceTypeId();
        if (resourceTypeId != null) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            return dao.getResourceTypeById(resourceTypeId);
        } else {
            return null;
        }
    }

    @Nullable
    private static UnitType getPrecursorUnitType(
            Context context,
            ResourceType producedResourceType) {

        if ((producedResourceType == null)) {
            return null;
        }

        Integer unitTypeId = producedResourceType.getPrecursorUnitTypeId();
        if (unitTypeId != null) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            return dao.getUnitTypeById(unitTypeId);
        } else {
            return null;
        }
    }

    @Nullable
    private static UnitType getPrecursorUnitType(
            Context context,
            UnitType producedUnitType) {

        if ((producedUnitType == null)) {
            return null;
        }

        Integer unitTypeId = producedUnitType.getPrecursorUnitTypeId();
        if (unitTypeId != null) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            return dao.getUnitTypeById(unitTypeId);
        } else {
            return null;
        }
    }

    private static boolean debit(Context context, ResourceType resourceType, int productionAmount) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource resource = dao.getResourceByTypeId(resourceType.getId());

        if (resource.getAmount() >= productionAmount) {
            resource.setAmount(resource.getAmount() - productionAmount);
            dao.update(resource);
            return true;
        } else {
            Log.d(LOG_TAG, "debitResourceType: Building requires precursor: "
                    + " that the user is missing: " + resourceType.getName());
            return false;
        }
    }


    private static boolean debit(Context context, UnitType unitType, int productionAmount) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        List<Unit> units = dao.getUnitsByTypeId(unitType.getId());

        if ((units != null) && (units.size() >= productionAmount)) {
            for (int i = 0; i < productionAmount; i++) {
                dao.deleteUnit(units.get(0));
            }
            return true;
        } else {
            Log.d(LOG_TAG, "debitResourceType: Building requires precursor: "
                    + " that the user is missing: " + unitType.getName());
            return false;
        }
    }

    private static void credit(
            Context context,
            ResourceType producedResourceType,
            int productionAmount) {

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource producedResource = dao.getResourceByTypeId(producedResourceType.getId());

        if (producedResource == null) {
            producedResource = new Resource(producedResourceType.getId(), productionAmount);
            dao.insert(producedResource);
        } else {
            producedResource.setAmount(producedResource.getAmount() + productionAmount);
            dao.update(producedResource);
        }

        Log.d(LOG_TAG, "credit: Credited resource " + producedResourceType.getName());
    }

    private static void credit(Context context, UnitType unitType, int productionAmount) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        for (int i = 0; i < productionAmount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitType.getId());
            unit.setHealth(unitType.getHealth());
            unit.setLocatedAtBuildingId(null); // Null because the unit will be with the user.

            dao.insert(unit);
        }

        Log.d(LOG_TAG, "credit: Created " + productionAmount + " units of "
                + unitType.getName());
    }

    private static int getResourceAmount(Context context, @Nullable ResourceType resourceType) {
        if (resourceType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource resource = dao.getResourceByTypeId(resourceType.getId());

        return (resource != null) ? resource.getAmount() : 0;
    }

    private static int getUnitAmount(Context context, @Nullable UnitType unitType) {
        if (unitType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        List<Unit> units = dao.getUnitsByTypeId(unitType.getId());

        return (units != null) ? units.size() : 0;
    }
}
