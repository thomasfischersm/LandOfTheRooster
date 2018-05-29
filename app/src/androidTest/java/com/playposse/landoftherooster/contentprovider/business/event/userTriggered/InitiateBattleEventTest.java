package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
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
 * A test for {@link InitiateBattleEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitiateBattleEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent() throws InterruptedException {
        // Temporarily set the respawn time to 100ms.
        int originalRespawnDuration = GameConfig.BATTLE_RESPAWN_DURATION;
        GameConfig.BATTLE_RESPAWN_DURATION = 100;


        try {
            // Create building.
            long goblinCaveId = createGoblinCaveAndMarker(dao);

            // Create enough soldiers to be sure to win.
            createUnitsJoiningUser(dao, 10, SOLDIER_UNIT_TYPE_ID);

            // Trigger event.
            InitiateBattleEvent event = new InitiateBattleEvent(goblinCaveId);
            BusinessEngine.get()
                    .triggerEvent(event);

            // Assert battle result.
            BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
            Building building = buildingWithType.getBuilding();
            assertNotNull(building.getLastConquest());

            MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
            assertFalse(mapMarker.isReady());

            // Wait for the building to respawn.
            Thread.sleep(210);

            // Verify that the building respawned.
            buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
            building = buildingWithType.getBuilding();
            assertNull(building.getLastConquest());

            mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
            assertTrue(mapMarker.isReady());
        } finally {
            GameConfig.BATTLE_RESPAWN_DURATION = originalRespawnDuration;
        }
    }
}
