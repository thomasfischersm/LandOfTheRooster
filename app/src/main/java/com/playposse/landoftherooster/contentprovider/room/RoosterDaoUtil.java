package com.playposse.landoftherooster.contentprovider.room;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import java.util.List;

/**
 * A utility for common data operations on the {@link RoosterDao}.
 */
public final class RoosterDaoUtil {

    private static final String LOG_TAG = RoosterDaoUtil.class.getSimpleName();

    private RoosterDaoUtil() {}

    public static int getResourceAmount(Context context, @Nullable ResourceType resourceType) {
        if (resourceType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource resource = dao.getResourceByTypeId(resourceType.getId());

        return (resource != null) ? resource.getAmount() : 0;
    }

    public static int getUnitAmount(Context context, @Nullable UnitType unitType) {
        if (unitType == null) {
            return 0;
        }

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        List<Unit> units = dao.getUnitsByTypeId(unitType.getId());

        return (units != null) ? units.size() : 0;
    }

    public static void creditResource(
            Context context,
            int producedResourceTypeId,
            int productionAmount) {

        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        Resource producedResource = dao.getResourceByTypeId(producedResourceTypeId);

        if (producedResource == null) {
            producedResource = new Resource(producedResourceTypeId, productionAmount);
            dao.insert(producedResource);
        } else {
            producedResource.setAmount(producedResource.getAmount() + productionAmount);
            dao.update(producedResource);
        }

        Log.d(LOG_TAG, "creditUnit: Credited resource " + producedResourceTypeId);
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
    }}
