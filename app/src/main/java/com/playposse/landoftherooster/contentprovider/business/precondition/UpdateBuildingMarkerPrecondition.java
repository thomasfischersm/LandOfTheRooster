package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepository;
import com.playposse.landoftherooster.contentprovider.business.event.FreeItemProductionSucceededEvent;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionSucceededEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserPicksUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.List;

/**
 * A {@link BusinessPrecondition} that is triggered when an event might have affected the state
 * of a building. The precondition will determine which building markers have to be updated.
 */
public class UpdateBuildingMarkerPrecondition implements BusinessPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        final Item item;
        if (event instanceof UserPicksUpItemEvent) {
            item = ((UserPicksUpItemEvent) event).getItem();
        } else if (event instanceof UserDropsOffItemEvent) {
            item = ((UserDropsOffItemEvent) event).getItem();
        } else if (event instanceof ItemProductionSucceededEvent) {
            item = ((ItemProductionSucceededEvent) event).getProducedItem();
        } else if (event instanceof FreeItemProductionSucceededEvent) {
            item = ((FreeItemProductionSucceededEvent) event).getProducedItem();
        } else {
            throw new IllegalArgumentException("The event is of an unexpected type: "
                    + event.getClass().getName());
        }

        ProductionRuleRepository productionRuleRepository =
                ProductionRuleRepository.get(dataCache.getDao());
        List<BuildingWithType> affectedBuildings =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(item);

        if (affectedBuildings.size() > 0) {
            return new UpdateBuildingMarkerPreconditionOutcome(
                    true,
                    item,
                    affectedBuildings);
        } else {
            return new UpdateBuildingMarkerPreconditionOutcome(
                    false,
                    null,
                    null);
        }
    }
}