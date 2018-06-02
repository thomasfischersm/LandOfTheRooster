package com.playposse.landoftherooster.contentprovider.business.event.special;

import android.app.Activity;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import javax.annotation.Nullable;

/**
 * A special {@link BusinessEvent} that is intent for the currently running {@link Activity} to show
 * a dialog.
 */
public class ShowDialogEvent extends BusinessEvent {

    private final DialogType dialogType;
    @Nullable private final BuildingWithType buildingWithType;
    @Nullable private final String message;

    public ShowDialogEvent(
            DialogType dialogType,
            @Nullable BuildingWithType buildingWithType,
            @Nullable String message) {

        super((buildingWithType != null) ? buildingWithType.getBuilding().getId() : null);

        this.dialogType = dialogType;
        this.buildingWithType = buildingWithType;
        this.message = message;
    }


    public DialogType getDialogType() {
        return dialogType;
    }

    @Nullable
    public BuildingWithType getBuildingWithType() {
        return buildingWithType;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
