package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessEventListener;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneEnteredEvent;
import com.playposse.landoftherooster.contentprovider.business.event.special.DialogType;
import com.playposse.landoftherooster.contentprovider.business.event.special.ShowDialogEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test for {@link OpenBuildingDialogsAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OpenBuildingDialogsActionTest extends AbstractBusinessTest {

    @Test
    public void perform_showBuildingProductionDialog() {
        // Create wheat field.
        long wheatFieldId = createWheatFieldAndMarker(dao);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(wheatFieldId);

        // Prepare listener.
        CaptureDialogTypeListener listener = new CaptureDialogTypeListener();
        businessEngine.addEventListener(ShowDialogEvent.class, listener);

        // Test action.
        BuildingZoneEnteredEvent event = new BuildingZoneEnteredEvent(buildingWithType);
        businessEngine.triggerEvent(event);

        // Assert result.
        assertTrue(listener.isHasBeenTriggered());
        assertEquals(DialogType.BUILDING_PRODUCTION_DIALOG, listener.getCapturedDialogType());
    }

    @Test
    public void perform_showBattleDialog() {
        // Create goblin cave.
        long goblinCaveId = createGoblinCaveAndMarker(dao);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);

        // Prepare listener.
        CaptureDialogTypeListener listener = new CaptureDialogTypeListener();
        businessEngine.addEventListener(ShowDialogEvent.class, listener);

        // Test action.
        BuildingZoneEnteredEvent event = new BuildingZoneEnteredEvent(buildingWithType);
        businessEngine.triggerEvent(event);

        // Assert result.
        assertTrue(listener.isHasBeenTriggered());
        assertEquals(DialogType.BATTLE_BUILDING_DIALOG, listener.getCapturedDialogType());
    }

    @Test
    public void perform_showBattleRespawnDialog() {
        // Create goblin cave.
        long goblinCaveId = createGoblinCaveAndMarker(dao);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(goblinCaveId);

        // Set battle as recently happened.
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(new Date());
        dao.update(building);

        // Prepare listener.
        CaptureDialogTypeListener listener = new CaptureDialogTypeListener();
        businessEngine.addEventListener(ShowDialogEvent.class, listener);

        // Test action.
        BuildingZoneEnteredEvent event = new BuildingZoneEnteredEvent(buildingWithType);
        businessEngine.triggerEvent(event);

        // Assert result.
        assertTrue(listener.isHasBeenTriggered());
        assertEquals(DialogType.BATTLE_RESPAWN_DIALOG, listener.getCapturedDialogType());
    }

    @Test
    public void perform_showHealingBuildingDialog() {
        // Create wheat field.
        long hospitalId = createHospitalAndMarker(dao);
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(hospitalId);

        // Prepare listener.
        CaptureDialogTypeListener listener = new CaptureDialogTypeListener();
        businessEngine.addEventListener(ShowDialogEvent.class, listener);

        // Test action.
        BuildingZoneEnteredEvent event = new BuildingZoneEnteredEvent(buildingWithType);
        businessEngine.triggerEvent(event);

        // Assert result.
        assertTrue(listener.isHasBeenTriggered());
        assertEquals(DialogType.HEALING_BUILDING_DIALOG, listener.getCapturedDialogType());
    }

    /**
     * A {@link BusinessEventListener} that captures the {@link DialogType} of
     * {@link ShowDialogEvent}s.
     */
    private class CaptureDialogTypeListener implements BusinessEventListener {

        private boolean hasBeenTriggered = false;
        private DialogType capturedDialogType;

        @Override
        public void onEvent(BusinessEvent event, BusinessDataCache cache) {
            hasBeenTriggered = true;
            capturedDialogType = ((ShowDialogEvent) event).getDialogType();
        }

        public boolean isHasBeenTriggered() {
            return hasBeenTriggered;
        }

        public DialogType getCapturedDialogType() {
            return capturedDialogType;
        }
    }
}
