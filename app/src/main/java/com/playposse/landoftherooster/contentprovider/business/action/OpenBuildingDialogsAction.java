package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.analytics.Analytics;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.special.DialogType;
import com.playposse.landoftherooster.contentprovider.business.event.special.ShowDialogEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

/**
 * An {@link BusinessAction} that opens building dialogs by triggering a {@link ShowDialogEvent}.
 */
public class OpenBuildingDialogsAction implements BusinessAction {
    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        BuildingWithType buildingWithType = dataCache.getBuildingWithType();
        Building building = dataCache.getBuilding();
        BuildingType buildingType = dataCache.getBuildingType();
        BusinessEngine businessEngine = BusinessEngine.get();

        if (buildingType.getEnemyUnitCount() != null) {
            if (building.getLastConquest() == null) {
                // Show battle dialog.
                ShowDialogEvent dialogEvent = new ShowDialogEvent(
                        DialogType.BATTLE_BUILDING_DIALOG,
                        buildingWithType,
                        null);
                businessEngine.triggerDelayedEvent(dialogEvent);

                // Report event to analytics.
                Analytics.reportEvent(Analytics.AppEvent.SHOW_BATTLE_BUILDING_DIALOG);
            } else {
                // Show battle respawn dialog.
                ShowDialogEvent dialogEvent = new ShowDialogEvent(
                        DialogType.BATTLE_RESPAWN_DIALOG,
                        buildingWithType,
                        null);
                businessEngine.triggerDelayedEvent(dialogEvent);

                // Report event to analytics.
                Analytics.reportEvent(Analytics.AppEvent.SHOW_BATTLE_RESPAWN_DIALOG);
            }
        } else if (buildingType.isHealsUnits()) {
            // Show healing building dialog.
            ShowDialogEvent dialogEvent = new ShowDialogEvent(
                    DialogType.HEALING_BUILDING_DIALOG,
                    buildingWithType,
                    null);
            businessEngine.triggerDelayedEvent(dialogEvent);

            // Report event to analytics.
            Analytics.reportEvent(Analytics.AppEvent.SHOW_HEALING_BUILDING_DIALOG);
        } else {
            // Show building production dialog.
            ShowDialogEvent dialogEvent = new ShowDialogEvent(
                    DialogType.BUILDING_PRODUCTION_DIALOG,
                    buildingWithType,
                    null);
            businessEngine.triggerDelayedEvent(dialogEvent);

            // Report event to analytics.
            Analytics.reportEvent(Analytics.AppEvent.SHOW_PRODUCTION_BUILDING_DIALOG);
        }
    }
}
