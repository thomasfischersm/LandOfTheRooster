package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.RespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.combat.Battle;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

/**
 * A {@link BusinessAction} that executes a Battle. After it completes, it fires a
 * {@link PostBattleEvent}.
 */
public class ExecuteBattleAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        long buildingId = buildingWithType.getBuilding().getId();
        RoosterDao dao = dataCache.getDao();

        // Perform battle.
        Battle battle = new Battle(dao, buildingWithType);
        BattleSummaryParcelable battleSummary = battle.fight();

        // Fire post event.
        PostBattleEvent postBattleEvent =
                new PostBattleEvent(buildingWithType, battleSummary);
        BusinessEngine.get()
                .triggerDelayedEvent(postBattleEvent);

        // Schedule event to respawn the building.
        RespawnBattleBuildingEvent.schedule(buildingId);
    }
}
