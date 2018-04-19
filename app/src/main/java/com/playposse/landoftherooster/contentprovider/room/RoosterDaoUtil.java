package com.playposse.landoftherooster.contentprovider.room;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import java.util.List;

/**
 * A utility for common data operations on the {@link RoosterDao}.
 */
public final class RoosterDaoUtil {

    private static final String LOG_TAG = RoosterDaoUtil.class.getSimpleName();

    public static final int PRODUCTION_CYCLE_MINUTES = 60;
    public static final int PRODUCTION_CYCLE_MS = PRODUCTION_CYCLE_MINUTES * 60 * 1_000;

    private RoosterDaoUtil() {
    }

    public static int getResourceAmount(Context context, @Nullable ResourceType resourceType) {
        if (resourceType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource resource = dao.getResourceJoiningUserByTypeId(resourceType.getId());

        return (resource != null) ? resource.getAmount() : 0;
    }

    public static int getUnitAmount(Context context, @Nullable UnitType unitType) {
        if (unitType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        List<Unit> units = dao.getUnitsJoiningUserByTypeId(unitType.getId());

        return (units != null) ? units.size() : 0;
    }

    public static void creditResource(
            Context context,
            int resourceTypeId,
            int amount,
            @Nullable Integer buildingId) {

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        final ResourceWithType resourceWithType;
        if (buildingId == null) {
            resourceWithType = dao.getResourceWithTypeJoiningUser(resourceTypeId);
        } else {
            resourceWithType = dao.getResourceWithType(resourceTypeId, buildingId);
        }

        Resource resource = (resourceWithType != null) ? resourceWithType.getResource() : null;
        creditResource(context, resource, resourceTypeId, amount, buildingId);
    }

    public static void creditResource(
            Context context,
            Resource resource,
            int resourceTypeId,
            int amount,
            @Nullable Integer buildingId) {

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        if (resource == null) {
            resource = new Resource(resourceTypeId, amount, buildingId);
            dao.insert(resource);
        } else {
            int newTotal = resource.getAmount() + amount;
            if (newTotal > 0) {
                resource.setAmount(newTotal);
                dao.update(resource);
            } else {
                dao.delete(resource);
            }
        }

        Log.d(LOG_TAG, "creditUnit: Credited resourceWithType " + resourceTypeId);
    }

    public static void creditUnit(Context context, UnitType unitType, int productionAmount) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        for (int i = 0; i < productionAmount; i++) {
            Unit unit = new Unit();
            unit.setUnitTypeId(unitType.getId());
            unit.setHealth(unitType.getHealth());
            unit.setLocatedAtBuildingId(null); // Null because the unit will be with the user.

            dao.insert(unit);
        }

        Log.d(LOG_TAG, "creditUnit: Created " + productionAmount + " units of "
                + unitType.getName());
    }

    public static void moveResourceToBuilding(
            Context context,
            ResourceWithType userResourceWithType,
            ResourceWithType buildingResourceWithType,
            int resourceTypeId,
            int buildingId) {

        if ((userResourceWithType == null)
                || (userResourceWithType.getResource().getAmount() < 1)) {
            throw new IllegalStateException(
                    "The user doesn't have the resource to move to the building.");
        }

        creditResource(
                context,
                userResourceWithType.getResource(),
                resourceTypeId,
                -1,
                null);

        creditResource(
                context,
                buildingResourceWithType.getResource(),
                resourceTypeId,
                1,
                buildingId);
    }

    public static void moveResourceFromBuilding(
            Context context,
            ResourceWithType userResourceWithType,
            ResourceWithType buildingResourceWithType,
            int resourceTypeId,
            int buildingId) {

        if ((buildingResourceWithType == null)
                || (buildingResourceWithType.getResource().getAmount() < 1)) {
            throw new IllegalStateException(
                    "The building doesn't have the resource to move to the building.");
        }

        creditResource(
                context,
                userResourceWithType.getResource(),
                resourceTypeId,
                1,
                null);

        creditResource(
                context,
                buildingResourceWithType.getResource(),
                resourceTypeId,
                -1,
                buildingId);
    }

    public static void transferUnitToBuilding(Context context, int unitTypeId, int buildingId) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        List<Unit> units = dao.getUnitsJoiningUserByTypeId(unitTypeId);

        if ((units == null) || (units.size() < 1)) {
            throw new IllegalStateException("The user cannot transfer a unit to the building " +
                    "because there is no such unit: " + unitTypeId);
        }

        Unit unit = units.get(0);

        if (unit.getLocatedAtBuildingId() != null) {
            throw new IllegalStateException("The unit is already at a building! " + unit.getId());
        }

        unit.setLocatedAtBuildingId(buildingId);
        dao.update(unit);
    }

    public static void transferUnitFromBuilding(Context context, int unitTypeId, int buildingId) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();

        List<Unit> units = dao.getUnits(unitTypeId, buildingId);

        if ((units == null) || (units.size() < 1)) {
            throw new IllegalStateException("The user cannot transfer a unit from the building " +
                    "because there is no such unit: " + unitTypeId);
        }

        Unit unit = units.get(0);

        if (unit.getLocatedAtBuildingId() == null) {
            throw new IllegalStateException(
                    "The unit is already joining the user! " + unit.getId());
        }

        unit.setLocatedAtBuildingId(null);
        dao.update(unit);
    }

    public static int getProductionSpeedInMinutes(int peasantCount) {
        if (peasantCount > 0) {
            return PRODUCTION_CYCLE_MINUTES / peasantCount;
        } else {
            throw new IllegalStateException("Can't calculate production speed for peasant count: "
                    + peasantCount);
        }
    }
}
