package com.playposse.landoftherooster.contentprovider.parser;

import java.util.List;

/**
 * A GSON class to read the production rules.
 */
public class ProductionRuleRoot {

    private List<ProductionRule> productionRules;

    public List<ProductionRule> getProductionRules() {
        return productionRules;
    }
}
