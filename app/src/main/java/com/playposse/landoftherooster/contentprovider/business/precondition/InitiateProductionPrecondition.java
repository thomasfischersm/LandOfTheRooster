package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;
import java.util.Map;

/**
 * A {@link BusinessPrecondition} that checks if the building can execute a production rule.
 */
public class InitiateProductionPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = InitiateProductionPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        Building building = dataCache.getBuilding();
        if (building == null) {
            throw new IllegalStateException("The building should have been available.");
        }

        // Skip if already started.
        if (building.getProductionStart() != null) {
            Log.i(LOG_TAG, "evaluate: Won't start new building production because it is " +
                    "already started.");
            return new InitiateProductionPreconditionOutcome(
                    false,
                    null,
                    null);
        }

        // Skip if no non-free production rule.
        if (!hasNonFreeProductionRule(dataCache)) {
            Log.i(LOG_TAG, "evaluate: The building " + dataCache.getBuildingId()
                    + " doesn't have a non-free production rule. Won't start production!");
            return new InitiateProductionPreconditionOutcome(
                    false,
                    null,
                    null);
        }

        // Check inputs.
        InitiateProductionPreconditionOutcome outcome = computePossibleProductionCount(dataCache);
        if (!outcome.getSuccess()) {
            Log.i(LOG_TAG, "evaluate: Won't start building production because the " +
                    "prerequisites are incomplete.");
        }

        return outcome;
    }

    private static boolean hasNonFreeProductionRule(BusinessDataCache dataCache) {
        List<ProductionRule> productionRules = dataCache.getProductionRules();
        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                if (!productionRule.isFree()) {
                    return true;
                }
            }
        }

        return false;
    }

    protected InitiateProductionPreconditionOutcome computePossibleProductionCount(
            BusinessDataCache dataCache) {

        int count = 0;
        ProductionRule chosenProductionRule = null;

        List<ProductionRule> productionRules = dataCache.getProductionRules();
        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                if (!productionRule.isFree()) {
                    count += computePossibleProductionCount(productionRule, dataCache);

                    if ((count > 0) && (chosenProductionRule == null)) {
                        chosenProductionRule = productionRule;
                    }
                }
            }
        }

        if (count > 0) {
            return new InitiateProductionPreconditionOutcome(
                    true,
                    chosenProductionRule,
                    count);
        } else {
            return new InitiateProductionPreconditionOutcome(
                    false,
                    null,
                    null);
        }
    }

    private int computePossibleProductionCount(
            ProductionRule productionRule,
            BusinessDataCache dataCache) {

        Integer count = null;

        Map<Long, Integer> resourceMap = dataCache.getResourceMap();
        Map<Long, Integer> unitMap = dataCache.getUnitMap();

        if ((resourceMap == null) || (unitMap == null)) {
            throw new IllegalStateException("Expected to get a resource and unit map.");
        }

        // Check resource inputs.
        List<Long> inputResourceTypeIds = productionRule.getSplitInputResourceTypeIds();
        for (long resourceTypeId : inputResourceTypeIds) {
            Integer resourceCount = resourceMap.get(resourceTypeId);
            if ((resourceCount == null) || (resourceCount < 1)) {
                return 0;
            } else {
                if (count == null) {
                    count = resourceCount;
                } else {
                    count = Math.min(count, resourceCount);
                }
            }
        }

        // Check unit inputs.
        List<Long> inputUnitTypeIds = productionRule.getSplitInputUnitTypeIds();
        for (long unitTypeId : inputUnitTypeIds) {
            Integer unitCount = unitMap.get(unitTypeId);
            if ((unitCount == null) || (unitCount < 1)) {
                return 0;
            } else {
                if (count == null) {
                    count = unitCount;
                } else {
                    count = Math.min(count, unitCount);
                }
            }
        }

        return count;
    }
}
