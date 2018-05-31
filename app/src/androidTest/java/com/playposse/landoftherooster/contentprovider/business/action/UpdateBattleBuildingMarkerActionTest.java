package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostBattleEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link UpdateBattleBuildingMarkerAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UpdateBattleBuildingMarkerActionTest extends AbstractBusinessTest {

    @Test
    public void perform_afterBattle() {
        // Create goblin cave.
        long goblinCaveId = createGoblinCaveAndMarker(dao);
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building goblinCave = buildingWithType.getBuilding();
        goblinCave.setLastConquest(new Date());
        dao.update(goblinCave);

        // Perform action.
        PostBattleEvent event = new PostBattleEvent(buildingWithType, null);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        PreconditionOutcome outcome = new PreconditionOutcome(true);
        UpdateBattleBuildingMarkerAction action = new UpdateBattleBuildingMarkerAction();
        action.perform(event, outcome, cache);

        // Assert outcome.
        MapMarker resultMapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        assertFalse(resultMapMarker.isReady());
        assertEquals((Integer) 0, resultMapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, resultMapMarker.getCompletedProductionCount());

        // Assert that the modification date is updated.
        long originalLastModified = mapMarker.getLastModified().getTime();
        long resultLastModified = resultMapMarker.getLastModified().getTime();
        assertTrue(resultLastModified > originalLastModified);

    }

    @Test
    public void perform_afterRespawn() {
        // Create goblin cave.
        long goblinCaveId = createGoblinCaveAndMarker(dao);
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building goblinCave = buildingWithType.getBuilding();
        goblinCave.setLastConquest(null);
        dao.update(goblinCave);

        // Perform action.
        PostBattleEvent event = new PostBattleEvent(buildingWithType, null);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        PreconditionOutcome outcome = new PreconditionOutcome(true);
        UpdateBattleBuildingMarkerAction action = new UpdateBattleBuildingMarkerAction();
        action.perform(event, outcome, cache);

        // Assert outcome.
        MapMarker resultMapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        assertTrue(resultMapMarker.isReady());
        assertEquals((Integer) 0, resultMapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, resultMapMarker.getCompletedProductionCount());

        // Assert that the modification date is updated.
        long originalLastModified = mapMarker.getLastModified().getTime();
        long resultLastModified = resultMapMarker.getLastModified().getTime();
        assertTrue(resultLastModified > originalLastModified);
    }
}
