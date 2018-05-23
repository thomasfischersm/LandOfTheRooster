package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.FreeItemProductionSucceededEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.FreeProductionPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;

/**
 * Created by thoma on 5/4/2018.
 */
public class FreeProductionAction extends ProductionAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        FreeProductionPreconditionOutcome outcome =
                (FreeProductionPreconditionOutcome) preconditionOutcome;
        List<ProductionRule> productionRules = outcome.getProductionRules();

        if (productionRules == null) {
            throw new  NullPointerException(
                    "FreeProductionAction encountered null production rules!");
        }

        // TODO: Consider if only one rule should succeed.
        Item producedItem = null;
        for (ProductionRule productionRule : productionRules) {
            producedItem = produce(productionRule, dataCache);
        }

        BusinessEngine.get().triggerDelayedEvent(
                new FreeItemProductionSucceededEvent(event.getBuildingId(), producedItem));
    }
}
