package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionSucceededEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.ProductionPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;
import java.util.Map;

/**
 * A {@link BusinessAction} that is fired when the item production has reached its final time.
 * It consumes and produces the actual resources that the production rule specifies.
 */
public class ProductionAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        ProductionPreconditionOutcome outcome =
                (ProductionPreconditionOutcome) preconditionOutcome;
        ProductionRule productionRule = outcome.getProductionRule();
        if (productionRule == null) {
            throw new NullPointerException("The precondition should have found a ProductionRule.");
        }

        Item producedItem = produce(productionRule, dataCache);

        BusinessEngine.get().triggerEvent
                (new ItemProductionSucceededEvent(event.getBuildingId(), producedItem));
    }

    protected Item produce(ProductionRule productionRule, BusinessDataCache dataCache) {
        RoosterDao dao = dataCache.getDao();
        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Building building = buildingWithType.getBuilding();
        long buildingId = building.getId();
        Map<Long, Integer> resourceMap = dataCache.getResourceMap();
        Map<Long, Integer> unitMap = dataCache.getUnitMap();
        Item producedItem = null;

        // Debit input resources.
        List<Long> inputResourceTypeIds = productionRule.getSplitInputResourceTypeIds();
        if (inputResourceTypeIds != null) {
            for (Long inputResourceTypeId : inputResourceTypeIds) {
                // TODO: Optimize by avoiding to have to load the Resourced again from the dao.
                RoosterDaoUtil.creditResource(dao, inputResourceTypeId, -1, buildingId);
            }
        }

        // Debit input units.
        List<Long> inputUnitTypeIds = productionRule.getSplitInputUnitTypeIds();
        if (inputUnitTypeIds != null) {
            for (Long inputUnitTypeId : inputUnitTypeIds) {
                RoosterDaoUtil.creditUnit(dao, inputUnitTypeId, -1, buildingId);
            }
        }

        // Credit output resource.
        Long outputResourceTypeId = productionRule.getOutputResourceTypeId();
        if (outputResourceTypeId != null) {
            RoosterDaoUtil.creditResource(
                    dao,
                    outputResourceTypeId,
                    1,
                    buildingId);
            producedItem = new ResourceItem(outputResourceTypeId);
        }

        // Credit output unit.
        Long outputUnitTypeId = productionRule.getOutputUnitTypeId();
        if (outputUnitTypeId != null) {
            RoosterDaoUtil.creditUnit(
                    dao,
                    outputUnitTypeId,
                    1,
                    buildingId);
            producedItem = new UnitItem(outputUnitTypeId);
        }

        // Reset maps because resource and unit counts have updated. Following code may check if
        // there are enough items left to start another production round.
        dataCache.resetResourceMap();
        dataCache.resetUnitMap();

        // Clear production start.
        // TODO: Think if there is a way to avoid clearing it if another production cycle can start.
        building.setProductionStart(null);
        dao.update(building);

        if (producedItem == null) {
            throw new IllegalStateException("Failed to produce an item!");
        }

        return producedItem;
    }
}
