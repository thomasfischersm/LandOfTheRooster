package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.CompleteBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.event.RespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;
import com.playposse.landoftherooster.services.combat.Battle;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

/**
 * A {@link BusinessAction} that executes a Battle. After it completes, it fires a
 * {@link CompleteBattleEvent}.
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

        // Update building marker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(buildingId);
        mapMarker.setReady(!battleSummary.isDidFriendsWin());
        DaoEventRegistry.get(dao)
                .update(mapMarker);

        // Fire post event.
        CompleteBattleEvent completeBattleEvent =
                new CompleteBattleEvent(buildingWithType, battleSummary);
        BusinessEngine.get()
                .triggerDelayedEvent(completeBattleEvent);

        // Schedule event to respawn the building.
        int delayMs = GameConfig.BATTLE_RESPAWN_DURATION;
        RespawnBattleBuildingEvent respawnEvent = new RespawnBattleBuildingEvent(buildingId);
        BusinessEngine.get()
                .scheduleEvent(delayMs, respawnEvent);
    }
}
