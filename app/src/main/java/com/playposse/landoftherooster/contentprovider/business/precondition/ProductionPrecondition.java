package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.annotation.NonNull;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.Map;

/**
 * A {@link BusinessPrecondition} that checks if the production of an item can be completed.
 *
 * <p>It uses the {@link InitiateProductionPrecondition} for most of the checks. Additionally, it
 * checks if the time is ready.
 */
public class ProductionPrecondition extends InitiateProductionPrecondition {

    private static final String LOG_TAG = ProductionPrecondition.class.getSimpleName();

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {

        // Check inputs.
        InitiateProductionPreconditionOutcome startOutcome =
                computePossibleProductionCount(dataCache);
        if (!startOutcome.getSuccess()) {
            Log.i(LOG_TAG, "evaluate: Won't start building production because the " +
                    "prerequisites are incomplete.");
            return fail();
        }

        // Check that production has started.
        Building building = dataCache.getBuilding();
        if ((building == null) || (building.getProductionStart() == null)) {
            Log.i(LOG_TAG, "evaluate: Cannot produce because there is no production start!");
            return fail();
        }

        // Check that the production time has completed.
        Map<Long, Integer> unitMap = dataCache.getUnitMap();
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        if (unitMap == null) {
            throw new NullPointerException("UnitMap should not be null!!!");
        }
        Long duration = ProductionCycleUtil.getRemainingProductionTimeMs(unitMap, buildingWithType);
        if (duration == null) {
            throw new NullPointerException(
                    "ProductionCycleUtil.getRemainingProductionTimeMs failed!");
        }
        long productionEnd = building.getProductionStart().getTime() + duration;
        if (productionEnd > System.currentTimeMillis()) {
            // The production is not yet complete. Let's reschedule it.
            Log.i(LOG_TAG, "evaluate: Production for building " + dataCache.getBuildingId()
                    + " should have been complete, but it isn't!");
            CompleteProductionEvent completeEvent =
                    new CompleteProductionEvent(event.getBuildingId());
            BusinessEngine.get().scheduleEvent(duration, completeEvent);
            return fail();
        }

        // Everything checked out to start production.
        ProductionRule productionRule = startOutcome.getProductionRule();
        return new ProductionPreconditionOutcome(true, productionRule);
    }

    @NonNull
    private ProductionPreconditionOutcome fail() {
        return new ProductionPreconditionOutcome(false, null);
    }
}
