package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;

/**
 * A {@link BusinessPrecondition} that checks if the production of a free item can be started at a
 * certain building.
 */
public class StartFreeItemProductionPrecondition extends FreeProductionPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        List<ProductionRule> unblockedFreeProductionRules =
                getUnblockedFreeProductionRules(dataCache);

        if (unblockedFreeProductionRules.size() > 0) {
            return new StartFreeItemProductionPreconditionOutcome(
                    true,
                    unblockedFreeProductionRules);
        } else {
            return new StartFreeItemProductionPreconditionOutcome(false, null);
        }
    }
}
