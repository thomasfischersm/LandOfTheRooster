package com.playposse.landoftherooster.contentprovider.business.initializer;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link RespawnBattleStartupInitializer}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RespawnBattleStartupInitializerTest extends AbstractStartupInitializerTest {

    @Test
    public void scheduleIfNecessary() throws InterruptedException {
        // Create goblin cave.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Set respawn time.
        Building goblinCave = dao.getBuildingById(goblinCaveId);
        long lastConquestMs = System.currentTimeMillis() - GameConfig.BATTLE_RESPAWN_DURATION;
        goblinCave.setLastConquest(new Date(lastConquestMs));
        dao.update(goblinCave);

        // Start BusinessEngine.
        BusinessEngine.get()
                .start(targetContext);

        // Wait for schedule event to complete.
        waitForExecutedEventCount(2);

        // Assert that the battle has respawned.
        Building resultGoblinCave = dao.getBuildingById(goblinCaveId);
        assertNull(resultGoblinCave.getLastConquest());

        // Check MapMarker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());
    }
}
