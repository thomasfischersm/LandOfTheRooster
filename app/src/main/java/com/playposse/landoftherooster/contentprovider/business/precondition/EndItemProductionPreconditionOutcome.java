package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import javax.annotation.Nullable;

/**
 * The outcome for {@link EndItemProductionPrecondition}.
 */
public class EndItemProductionPreconditionOutcome extends PreconditionOutcome {

    @Nullable
    private final ProductionRule productionRule;

    public EndItemProductionPreconditionOutcome(
            Boolean isSuccess,
            @Nullable ProductionRule productionRule) {
        super(isSuccess);

        this.productionRule = productionRule;
    }

    @Nullable
    public ProductionRule getProductionRule() {
        return productionRule;
    }
}
