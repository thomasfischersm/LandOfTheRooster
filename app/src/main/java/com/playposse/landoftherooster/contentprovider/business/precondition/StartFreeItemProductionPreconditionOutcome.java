package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A {@link PreconditionOutcome} to start the production cycle for a free item.
 */
public class StartFreeItemProductionPreconditionOutcome extends PreconditionOutcome {

    @Nullable private final List<ProductionRule> productionRules;

    StartFreeItemProductionPreconditionOutcome(
            Boolean isSuccess,
            @Nullable List<ProductionRule> productionRules) {

        super(isSuccess);

        this.productionRules = productionRules;
    }

    @Nullable
    public List<ProductionRule> getProductionRules() {
        return productionRules;
    }
}
