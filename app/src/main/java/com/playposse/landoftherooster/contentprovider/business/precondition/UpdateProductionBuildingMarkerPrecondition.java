package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessPrecondition;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.List;

/**
 * A {@link BusinessPrecondition} that is triggered when an event might have affected the state
 * of a building. The precondition will determine which building markers have to be updated.
 */
public class UpdateProductionBuildingMarkerPrecondition implements BusinessPrecondition {

    @Override
    public PreconditionOutcome evaluate(BusinessEvent event, BusinessDataCache dataCache) {
        final Item item;
        if (event instanceof PostPickUpItemEvent) {
            item = ((PostPickUpItemEvent) event).getItem();
        } else if (event instanceof PostDropOffItemEvent) {
            item = ((PostDropOffItemEvent) event).getItem();
        } else if (event instanceof PostCompleteProductionEvent) {
            item = ((PostCompleteProductionEvent) event).getProducedItem();
        } else if (event instanceof PostCompleteFreeProductionEvent) {
            item = ((PostCompleteFreeProductionEvent) event).getProducedItem();
        } else if (event instanceof PostPickUpUnitFromHospitalEvent) {
            item = ((PostPickUpUnitFromHospitalEvent) event).getPickedUpUnitItem();
        } else {
            throw new IllegalArgumentException("The event is of an unexpected type: "
                    + event.getClass().getName());
        }

        ProductionRuleRepository productionRuleRepository =
                ProductionRuleRepository.get(dataCache.getDao());
        List<BuildingWithType> affectedBuildings =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(item);

        if (affectedBuildings.size() > 0) {
            return new UpdateProductionBuildingMarkerPreconditionOutcome(
                    true,
                    item,
                    affectedBuildings);
        } else {
            return new UpdateProductionBuildingMarkerPreconditionOutcome(
                    false,
                    null,
                    null);
        }
    }
}