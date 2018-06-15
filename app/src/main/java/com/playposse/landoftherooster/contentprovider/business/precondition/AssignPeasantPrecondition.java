package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;

import java.util.Map;

/**
 * A {@link BusinessPrecondition} that checks if the user can assign a peasant to a particular
 * building.
 */
public class AssignPeasantPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = AssignPeasantPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        // Does the user have a peasant joining?
        Map<Long, Integer> unitMapJoiningUser = dataCache.getUnitMapJoiningUser();
        Integer unitPeasantCount = unitMapJoiningUser.get(GameConfig.PEASANT_ID);
        if ((unitPeasantCount == null) || (unitPeasantCount < 1)) {
            Log.d(LOG_TAG, "evaluate: Can't assign peasant because the user doesn't have any " +
                    "peasants joining him/her.");
            return new PreconditionOutcome(false);
        }

        // Does the building have a free peasant slot?
        Map<Long, Integer> unitMapAtBuilding = dataCache.getUnitMap();
        Integer buildingPeasantCount = unitMapAtBuilding.get(GameConfig.PEASANT_ID);
        int impliedPeasantCount = GameConfig.IMPLIED_PEASANT_COUNT;
        int maxPeasantCount = GameConfig.MAX_PEASANT_BUILDING_CAPACITY;
        if ((buildingPeasantCount != null) &&
                (buildingPeasantCount + impliedPeasantCount >= maxPeasantCount)) {
            Log.d(LOG_TAG, "evaluate: Can't assign peasant because the building is already " +
                    "at capacity.");
            return new PreconditionOutcome(false);
        }

        return new PreconditionOutcome(true);
    }
}
