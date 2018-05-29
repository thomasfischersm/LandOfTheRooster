package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

/**
 * A {@link BusinessEvent} that is fired when a battle ends.
 */
public class CompleteBattleEvent extends BusinessEvent {

    private final BuildingWithType buildingWithType;
    private final BattleSummaryParcelable battleSummary;

    public CompleteBattleEvent(
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
}
