package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.util.List;

import javax.annotation.Nullable;

/**
 * The {@link PreconditionOutcome} for checking if a free item can be produced.
 */
public class FreeProductionPreconditionOutcome extends PreconditionOutcome {

    @Nullable private final List<ProductionRule> productionRules;

    public FreeProductionPreconditionOutcome(
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
