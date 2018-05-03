package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import javax.annotation.Nullable;

/**
 * Created by thoma on 5/2/2018.
 */
public class StartItemProductionPreconditionOutcome extends PreconditionOutcome {

    @Nullable
    private final ProductionRule productionRule;
    @Nullable
    private final Integer possibleProductionCount;

    StartItemProductionPreconditionOutcome(
            Boolean isSuccess,
            @Nullable ProductionRule productionRule,
            @Nullable Integer possibleProductionCount) {

        super(isSuccess);

        this.productionRule = productionRule;
        this.possibleProductionCount = possibleProductionCount;
    }

    @Nullable
    public ProductionRule getProductionRule() {
        return productionRule;
    }

    @Nullable
    public Integer getPossibleProductionCount() {
        return possibleProductionCount;
    }
}
