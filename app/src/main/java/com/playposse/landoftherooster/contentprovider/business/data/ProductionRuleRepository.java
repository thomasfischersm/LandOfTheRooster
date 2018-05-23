package com.playposse.landoftherooster.contentprovider.business.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Useful lookups for {@link ProductionRule}s. Looking up input resources and units can be tricky.
 * They are modeled as a string with a comma delimited list of ids in the database. This class
 * loads all the production rules and creates fast lookup tables for them.
 */
public final class ProductionRuleRepository {

    private static ProductionRuleRepository instance;

    private final RoosterDao dao;
    private final List<ProductionRule> productionRules = new ArrayList<>();
    private final Multimap<Long, ProductionRule> buildingTypeIdToProductionRuleMap =
            HashMultimap.create();
    private final Multimap<Long, ProductionRule> inputResourceTypeIdToProductionRuleMap =
            HashMultimap.create();
    private final Multimap<Long, ProductionRule> outputResourceTypeIdToProductionRuleMap =
            HashMultimap.create();
    private final Multimap<Long, ProductionRule> inputUnitTypeIdToProductionRuleMap =
            HashMultimap.create();
    private final Multimap<Long, ProductionRule> outputUnitTypeIdToProductionRuleMap =
            HashMultimap.create();

    private ProductionRuleRepository(RoosterDao dao) {
        this.dao = dao;

        init();
    }

    public static ProductionRuleRepository get(RoosterDao dao) {
        if (instance == null) {
            instance = new ProductionRuleRepository(dao);
        }
        return instance;
    }

    private void init() {
        // Access DB to load all production rules.
        productionRules.addAll(dao.getAllProductionRules());

        // Build inputResourceTypeIdToProductionRuleMap.
        for (ProductionRule productionRule : productionRules) {
            for (long inputResourceTypeId : productionRule.getSplitInputResourceTypeIds()) {
                inputResourceTypeIdToProductionRuleMap.put(inputResourceTypeId, productionRule);
            }

            Long outputResourceTypeId = productionRule.getOutputResourceTypeId();
            outputResourceTypeIdToProductionRuleMap.put(outputResourceTypeId, productionRule);

            buildingTypeIdToProductionRuleMap.put(
                    productionRule.getBuildingTypeId(),
                    productionRule);
        }

        // Build inputUnitTypeIdToProductionRuleMap.
        for (ProductionRule productionRule : productionRules) {
            for (long inputUnitTypeId : productionRule.getSplitInputUnitTypeIds()) {
                inputUnitTypeIdToProductionRuleMap.put(inputUnitTypeId, productionRule);
            }

            Long outputUnitTypeId = productionRule.getOutputUnitTypeId();
            outputUnitTypeIdToProductionRuleMap.put(outputUnitTypeId, productionRule);
        }
    }

    private List<ProductionRule> getSatisfiedProductionRules(
            Item item,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        final Collection<ProductionRule> rules;
        if (item instanceof ResourceItem) {
            long inputResourceTypeId = ((ResourceItem) item).getResourceTypeId();
            rules = inputResourceTypeIdToProductionRuleMap.get(inputResourceTypeId);
        } else if (item instanceof UnitItem) {
            long inputUnitTypeId = ((UnitItem) item).getUnitTypeId();
            rules = inputUnitTypeIdToProductionRuleMap.get(inputUnitTypeId);
        } else {
            throw new IllegalStateException("Item was of unexpected type: "
                    + item.getClass().getName());
        }

        List<ProductionRule> result = new ArrayList<>();
        for (ProductionRule productionRule : rules) {
            if (ProductionCycleUtil.hasProductionInputs(resourceMap, unitMap, productionRule)) { //Must consider building and user resources/units!
                result.add(productionRule);
            }
        }

        return result;
    }

    public List<ProductionRuleWithBuilding> getBuildingsWithSatisfiedPrerequisites(
            Item item,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        // Determine affected production rules.
        List<ProductionRule> rules =
                getSatisfiedProductionRules(item, resourceMap, unitMap);

        // Load buildings.
        List<BuildingWithType> buildingWithTypes =
                dao.getBuildingWithTypeByBuildingTypeIds(getBuildingTypeIds(rules));

        // Create lookup map for buildings.
        Map<Long, BuildingWithType> buildingTypeIdToBuildingWithTypeMap =
                new HashMap<>(buildingWithTypes.size());
        for (BuildingWithType buildingWithType : buildingWithTypes) {
            long buildingTypeId = buildingWithType.getBuildingType().getId();
            buildingTypeIdToBuildingWithTypeMap.put(buildingTypeId, buildingWithType);
        }

        // Create result.
        List<ProductionRuleWithBuilding> result = new ArrayList<>(rules.size());
        for (ProductionRule productionRule : rules) {
            long buildingTypeId = productionRule.getBuildingTypeId();
            BuildingWithType buildingWithType =
                    buildingTypeIdToBuildingWithTypeMap.get(buildingTypeId);
            result.add(new ProductionRuleWithBuilding(productionRule, buildingWithType));
        }

        return result;
    }

    private static List<Long> getBuildingTypeIds(List<ProductionRule> rules) {
        List<Long> buildingTypeIds = new ArrayList<>(rules.size());
        for (ProductionRule productionRule : rules) {
            buildingTypeIds.add(productionRule.getBuildingTypeId());
        }
        return buildingTypeIds;
    }

    public List<BuildingWithType> getBuildingsWithAffectedProductionRules(Item item) {
        // Evaluate items.
        List<Long> buildingTypeIds = new ArrayList<>();
        if (item instanceof ResourceItem) {
            ResourceItem resourceItem = (ResourceItem) item;
            buildingTypeIds.addAll(getBuildingTypeIdByProductionInput(resourceItem));
            buildingTypeIds.addAll(getBuildingTypeIdByProductionOutput(resourceItem));
        } else if (item instanceof UnitItem){
            UnitItem unitItem = (UnitItem) item;
            buildingTypeIds.addAll(getBuildingTypeIdByProductionInput(unitItem));
            buildingTypeIds.addAll(getBuildingTypeIdByProductionOutput(unitItem));
        } else {
            throw new IllegalArgumentException(
                    "Unexpected item type: " + item.getClass().getName());
        }

        // Convert production rules to buildings.
        return dao.getBuildingWithTypeByBuildingTypeIds(buildingTypeIds);
    }

    private List<Long> getBuildingTypeIdByProductionInput(ResourceItem item) {
        Collection<ProductionRule> productionRules =
                inputResourceTypeIdToProductionRuleMap.get(item.getResourceTypeId());
        return toBuildingTypeIds(productionRules);
    }

    private List<Long> getBuildingTypeIdByProductionOutput(ResourceItem item) {
        Collection<ProductionRule> productionRules =
                outputResourceTypeIdToProductionRuleMap.get(item.getResourceTypeId());
        return toBuildingTypeIds(productionRules);
    }

    private List<Long> getBuildingTypeIdByProductionInput(UnitItem item) {
        Collection<ProductionRule> productionRules =
                inputUnitTypeIdToProductionRuleMap.get(item.getUnitTypeId());
        return toBuildingTypeIds(productionRules);
    }

    private List<Long> getBuildingTypeIdByProductionOutput(UnitItem item) {
        Collection<ProductionRule> productionRules =
                outputUnitTypeIdToProductionRuleMap.get(item.getUnitTypeId());
        return toBuildingTypeIds(productionRules);
    }

    private List<Long> toBuildingTypeIds(Collection<ProductionRule> productionRules) {
        List<Long> buildingTypeIds = new ArrayList<>(productionRules.size());
        for (ProductionRule productionRule : productionRules) {
            buildingTypeIds.add(productionRule.getBuildingTypeId());
        }
        return buildingTypeIds;
    }

    public List<ProductionRule> getProductionRulesByBuildingTypeId(long buildingTypeId) {
        return new ArrayList<>(buildingTypeIdToProductionRuleMap.get(buildingTypeId));
    }

    /**
     * A helper data structure to keep a {@link ProductionRule} with its {@link BuildingWithType}.
     */
    public static class ProductionRuleWithBuilding {

        private final ProductionRule productionRule;
        private final BuildingWithType buildingWithType;

        private ProductionRuleWithBuilding(
                ProductionRule productionRule,
                BuildingWithType buildingWithType) {

            this.productionRule = productionRule;
            this.buildingWithType = buildingWithType;
        }

        public ProductionRule getProductionRule() {
            return productionRule;
        }

        public BuildingWithType getBuildingWithType() {
            return buildingWithType;
        }
    }
}
