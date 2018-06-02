package com.playposse.landoftherooster.dialog.support;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

/**
 * An {@link ActionData} to assign a peasant to a building
 */
public class UnitActionData extends ActionData {

    private final long unitTypeId;
    private final BuildingWithType buildingWithType;
    private final long buildingId;
    private final ActionType actionType;

    private int userUnitCount;
    private int buildingUnitCount;
    private UnitType unitType;

    public UnitActionData(
            Context context,
            RoosterDao dao,
            long unitTypeId,
            BuildingWithType buildingWithType,
            ActionType actionType) {

        super(context, dao);

        this.unitTypeId = unitTypeId;
        this.buildingWithType = buildingWithType;
        this.actionType = actionType;

        buildingId = buildingWithType.getBuilding().getId();

        // Load data.
        userUnitCount = dao.getUnitCountJoiningUser(unitTypeId);
        buildingUnitCount = dao.getUnitCount(unitTypeId, buildingId);
        unitType = dao.getUnitTypeById(unitTypeId);
    }

    @Override
    public String getUserString() {
        String unitTypeName = unitType.getName();
        return getContext().getString(R.string.unit_template, userUnitCount, unitTypeName);
    }

    @Override
    public String getBuildingString() {
        String unitTypeName = unitType.getName();
        return getContext().getString(R.string.unit_template, buildingUnitCount, unitTypeName);
    }

    @Override
    public String getActionString() {
        switch (actionType) {
            case DROP_OFF:
                return getContext().getString(R.string.drop_off_action);
            case PICKUP:
                return getContext().getString(R.string.pick_up_action);
            default:
                throw new IllegalStateException("Unexpected action type: " + actionType);
        }
    }

    @Override
    protected void performAction() {
        switch (actionType) {
            case DROP_OFF:
                DropOffItemEvent dropOffEvent =
                        new DropOffItemEvent(buildingId, new ResourceItem(unitTypeId));
                BusinessEngine.get()
                        .triggerEvent(dropOffEvent);
                break;
            case PICKUP:
                PickUpItemEvent pickUpEvent =
                        new PickUpItemEvent(buildingId, new ResourceItem(unitTypeId));
                BusinessEngine.get()
                        .triggerEvent(pickUpEvent);
                break;
            default:
                throw new IllegalStateException("Unexpected action type: " + actionType);
        }
    }

    @Override
    public boolean isAvailable() {
        switch (actionType) {
            case DROP_OFF:
                return userUnitCount > 0;
            case PICKUP:
                return buildingUnitCount > 0;
            default:
                throw new IllegalStateException("Unexpected action type: " + actionType);
        }
    }
}
