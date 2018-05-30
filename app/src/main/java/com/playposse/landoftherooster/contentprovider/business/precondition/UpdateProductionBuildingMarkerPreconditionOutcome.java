package com.playposse.landoftherooster.contentprovider.business.precondition;

import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.List;

/**
 * An {@link PreconditionOutcome} for {@link UpdateProductionBuildingMarkerPrecondition}.
 */
public class UpdateProductionBuildingMarkerPreconditionOutcome extends PreconditionOutcome {

    private final Item item;
    private final List<BuildingWithType> affectedBuildings;

    public UpdateProductionBuildingMarkerPreconditionOutcome(
            Boolean isSuccess,
            Item item,
            List<BuildingWithType> affectedBuildings) {

        super(isSuccess);

        this.item = item;
        this.affectedBuildings = affectedBuildings;
    }

    public Item getItem() {
        return item;
    }

    public List<BuildingWithType> getAffectedBuildingWithTypes() {
        return affectedBuildings;
    }
}
