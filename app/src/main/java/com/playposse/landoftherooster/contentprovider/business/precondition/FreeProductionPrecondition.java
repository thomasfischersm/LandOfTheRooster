package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.annotation.NonNull;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteFreeItemProduction;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A precondition to be checked if a free item can be produced.
 * <p>
 * <ol>
 * <li>
 * If the {@link BusinessEvent} is a new building production, the condition is satisfied if there is
 * a free production rule.
 * </li>
 * <li>
 * If the {@link BusinessEvent} is because the user has picked up the previously blocking item, a
 * certain amount of time has to have passed for the production to be complete.
 * </li>
 * </ol>
 */
public class FreeProductionPrecondition implements BusinessPrecondition {

    private static final String LOG_TAG = FreeProductionPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        if (event instanceof BuildingCreatedEvent) {
            return evaluateAfterBuildingConstruction(dataCache);
        } else if (event instanceof CompleteFreeItemProduction) {
            return evaluateAfterActualProduction(event, dataCache);
        } else {
            throw new IllegalStateException(
                    "FreeProductionPrecondition was triggered by an unexpected event: "
                            + event.getClass().getName());
        }
    }

    private PreconditionOutcome evaluateAfterBuildingConstruction(BusinessDataCache dataCache) {
        List<ProductionRule> freeProductionRules = getFreeProductionRules(dataCache);

        if (freeProductionRules.size() > 0) {
            return new FreeProductionPreconditionOutcome(true, freeProductionRules);
        } else {
            return new FreeProductionPreconditionOutcome(false, null);
        }
    }

    private PreconditionOutcome evaluateAfterActualProduction(
            BusinessEvent event,
            BusinessDataCache dataCache) {

        // Check if the production start date has completed.
        Building building = dataCache.getBuilding();
        if ((building == null)) {
            throw new NullPointerException("Building should not be null!");
        }
        if ((building.getProductionStart() == null)) {
            Log.i(LOG_TAG, "evaluateAfterActualProduction: Canceling production. The " +
                    "production start hasn't been set!");
            return fail();
        }
        Map<Long, Integer> unitMap = dataCache.getUnitMap();
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Long delayMs = ProductionCycleUtil.getRemainingProductionTimeMs(unitMap, buildingWithType);
        if ((delayMs == null) || (delayMs > 0)) {
            Log.i(LOG_TAG, "evaluateAfterActualProduction: Free production is not ready yet, " +
                    "scheduling it for later: " + delayMs);
            BusinessEngine.get().scheduleEvent(delayMs, event);
            return fail();
        }

        // Find unblocked free production rules.
        List<ProductionRule> unblockedFreeProductionRules =
                getUnblockedFreeProductionRules(dataCache);
        return new FreeProductionPreconditionOutcome(true, unblockedFreeProductionRules);
    }

    @NonNull
    private FreeProductionPreconditionOutcome fail() {
        return new FreeProductionPreconditionOutcome(false, null);
    }

    private List<ProductionRule> getFreeProductionRules(BusinessDataCache dataCache) {
        List<ProductionRule> productionRules = dataCache.getProductionRules();
        List<ProductionRule> freeProductionRules = new ArrayList<>();

        if (productionRules != null) {
            for (ProductionRule productionRule : productionRules) {
                if (productionRule.isFree()) {
                    freeProductionRules.add(productionRule);
                }
            }
        }

        return freeProductionRules;
    }

    protected List<ProductionRule> getUnblockedFreeProductionRules(BusinessDataCache dataCache) {
        List<ProductionRule> productionRules = dataCache.getProductionRules();
        List<ProductionRule> unblockedFreeProductionRules = new ArrayList<>();
        Map<Long, Integer> resourceMap = dataCache.getResourceMap();
        Map<Long, Integer> unitMap = dataCache.getUnitMap();

        if ((productionRules != null) && (resourceMap != null) && (unitMap != null)) {
            for (ProductionRule productionRule : productionRules) {
                if (productionRule.isFree()) {
                    if (productionRule.getOutputResourceTypeId() != null) {
                        Integer resourceCount =
                                resourceMap.get(productionRule.getOutputResourceTypeId());
                        if ((resourceCount == null) || (resourceCount == 0)) {
                            unblockedFreeProductionRules.add(productionRule);
                        }
                    } else if (productionRule.getOutputUnitTypeId() != null) {
                        Integer unitCount = unitMap.get(productionRule.getOutputUnitTypeId());
                        if ((unitCount == null) || (unitCount == 0)) {
                            unblockedFreeProductionRules.add(productionRule);
                        }
                    } else {
                        throw new IllegalStateException("Production rule needs to have at least " +
                                "one output: " + productionRule.getId());
                    }
                }
            }
        }

        return unblockedFreeProductionRules;
    }
}
