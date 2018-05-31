package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.InitiateBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.ExecuteBattlePreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link ExecuteBattleAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExecuteBattleActionTest extends AbstractBusinessTest {

    @Test
    public void perform_successfulBattle() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Create enough units to be sure to win.
        createUnitsJoiningUser(dao, 10, SOLDIER_UNIT_TYPE_ID);

        // Execute action.
        InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        ExecuteBattlePreconditionOutcome outcome =
                new ExecuteBattlePreconditionOutcome(true);
        ExecuteBattleAction action = new ExecuteBattleAction();
        action.perform(event, outcome, cache);

        // Verify outcome.
        // TODO: Attach to PostBattleEvent to see the outcome.

        // Verify building.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        assertNotNull(building.getLastConquest());
        assertTrue(building.getLastConquest().getTime() < System.currentTimeMillis());
        assertTrue(
                building.getLastConquest().getTime()
                        + GameConfig.BATTLE_RESPAWN_DURATION
                        > System.currentTimeMillis());

        // Verify marker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        assertFalse(mapMarker.isReady());
    }

    @Test
    public void perform_defeatedBattle() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Create no units to ensure a defeat.

        // Execute action.
        InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        ExecuteBattlePreconditionOutcome outcome =
                new ExecuteBattlePreconditionOutcome(true);
        ExecuteBattleAction action = new ExecuteBattleAction();
        action.perform(event, outcome, cache);

        // Verify outcome.
        // TODO: Attach to PostBattleEvent to see the outcome.

        // Verify building.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        assertNull(building.getLastConquest());
    }
}
