package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.Item;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

/**
 * A {@link BusinessEvent} that is fired when a battle ends.
 */
public class PostBattleEvent extends BusinessEvent {

    private final BuildingWithType buildingWithType;
    private final BattleSummaryParcelable battleSummary;

    public PostBattleEvent(
            BuildingWithType buildingWithType,
            BattleSummaryParcelable battleSummary) {

        super(buildingWithType.getBuilding().getId());

        this.buildingWithType = buildingWithType;
        this.battleSummary = battleSummary;
    }

    public BuildingWithType getBuildingWithType() {
        return buildingWithType;
    }

    public BattleSummaryParcelable getBattleSummary() {
        return battleSummary;
    }

    public Item getConquestPrizeItem() {
        Integer prizeResourceTypeId =
                buildingWithType.getBuildingType().getConquestPrizeResourceTypeId();
        return new ResourceItem(prizeResourceTypeId);
    }
}
