package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.InitiateBattleEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link ExecuteBattlePrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExecuteBattlePreconditionTest extends AbstractBusinessTest {

    @Test
    public void evaluate_failed_notYetRespawned() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Set respawn timeout.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(new Date());
        dao.update(building);

        // Try precondition
        InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        ExecuteBattlePrecondition precondition = new ExecuteBattlePrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_failed_notABattleBuilding() {
        // Create building.
        long wheatFieldId = createWheatFieldAndMarker(dao);

        // Try precondition
        InitiateBattleEvent event = new InitiateBattleEvent(wheatFieldId);
        BusinessDataCache cache = new BusinessDataCache(dao, wheatFieldId);
        ExecuteBattlePrecondition precondition = new ExecuteBattlePrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertFalse(outcome.getSuccess());
    }

    @Test
    public void evaluate_succeeded_timeReady() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Set respawn timeout.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        long respawnTime = System.currentTimeMillis() - GameConfig.BATTLE_RESPAWN_DURATION;
        building.setLastConquest(new Date(respawnTime));
        dao.update(building);

        // Try precondition
        InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        ExecuteBattlePrecondition precondition = new ExecuteBattlePrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertTrue(outcome.getSuccess());
    }

    @Test
    public void evaluate_succeeded_alreadyRespawned() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Set respawn timeout.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(null);
        dao.update(building);

        // Try precondition
        InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        ExecuteBattlePrecondition precondition = new ExecuteBattlePrecondition();
        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        // Assert result.
        assertTrue(outcome.getSuccess());
    }
}
