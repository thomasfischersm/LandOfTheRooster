package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.RespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.RespawnBattleBuildingPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link RespawnBattleBuildingAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RespawnBattleBuildingActionTest extends AbstractBusinessTest {

    @Test
    public void perform() {
        // Create building.
        long goblinCaveId = createGoblinCaveAndMarker(dao);

        // Set conquest date.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(new Date());
        dao.update(building);

        // Ensure that the marker is NOT ready.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        mapMarker.setReady(false);
        dao.update(mapMarker);

        // Execute the action
        RespawnBattleBuildingEvent event = new RespawnBattleBuildingEvent(goblinCaveId);
        RespawnBattleBuildingPreconditionOutcome outcome =
                new RespawnBattleBuildingPreconditionOutcome(true);
        BusinessDataCache cache = new BusinessDataCache(dao, goblinCaveId);
        RespawnBattleBuildingAction action = new RespawnBattleBuildingAction();
        action.perform(event, outcome, cache);

        // Assert the building.
        BuildingWithType resultBuildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);
        Building resultBuilding = resultBuildingWithType.getBuilding();
        assertNull(resultBuilding.getLastConquest());

        // Assert marker.
        MapMarker resultMapMarker = dao.getMapMarkerByBuildingId(goblinCaveId);
        assertTrue(resultMapMarker.isReady());
        long originalLastModified = mapMarker.getLastModified().getTime();
        long resultLastModified = resultMapMarker.getLastModified().getTime();
        assertTrue(resultLastModified > originalLastModified);
    }
}
