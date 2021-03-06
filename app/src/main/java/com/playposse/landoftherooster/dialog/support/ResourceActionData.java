package com.playposse.landoftherooster.dialog.support;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

/**
 * An {@link ActionData} for a resource.
 */
public class ResourceActionData extends ActionData {

    private final long resourceTypeId;
    private final BuildingWithType buildingWithType;
    private final long buildingId;
    private final ActionType actionType;

    private ResourceWithType userResourceWithType;
    private ResourceWithType buildingResourceWithType;
    private ResourceType resourceType;

    public ResourceActionData(
            Context context,
            RoosterDao dao,
            long resourceTypeId,
            BuildingWithType buildingWithType,
            ActionType ActionType) {

        super(context, dao);

        this.resourceTypeId = resourceTypeId;
        this.buildingWithType = buildingWithType;
        this.actionType = ActionType;

        buildingId = buildingWithType.getBuilding().getId();

        // Load data.
        userResourceWithType = dao.getResourceWithTypeJoiningUser(resourceTypeId);
        buildingResourceWithType = dao.getResourceWithType(resourceTypeId, buildingId);

        // Try to avoid an extra query for ResourceType.
        if (userResourceWithType != null) {
            resourceType = userResourceWithType.getType();
        } else if (buildingResourceWithType != null) {
            resourceType = buildingResourceWithType.getType();
        } else {
            resourceType = dao.getResourceTypeById(resourceTypeId);
        }
    }

    @Override
    public String getUserString() {
        final int amount;
        if (userResourceWithType != null) {
            amount = userResourceWithType.getResource().getAmount();
        } else {
            amount = 0;
        }
        String resourceName = resourceType.getName();
        return getContext().getString(R.string.unit_template, amount, resourceName);
    }

    @Override
    public String getBuildingString() {
        final int amount;
        if (buildingResourceWithType != null) {
            amount = buildingResourceWithType.getResource().getAmount();
        } else {
            amount = 0;
        }
        String resourceName = resourceType.getName();
        return getContext().getString(R.string.unit_template, amount, resourceName);
    }

    @Override
    public String getActionString() {
        switch (actionType) {
            case DROP_OFF:
                return getContext().getString(R.string.drop_off_action);
            case PICKUP:
                return getContext().getString(R.string.pick_up_action);
            default:
                throw new IllegalStateException(
                        "Unexpected action type: " + actionType);
        }
    }

    @Override
    protected void performAction() {
        switch (actionType) {
            case DROP_OFF:
                DropOffItemEvent dropOffEvent =
                        new DropOffItemEvent(buildingId, new ResourceItem(resourceTypeId));
                BusinessEngine.get()
                        .triggerEvent(dropOffEvent);
                break;
            case PICKUP:
                PickUpItemEvent pickUpEvent =
                        new PickUpItemEvent(buildingId, new ResourceItem(resourceTypeId));
                BusinessEngine.get()
                        .triggerEvent(pickUpEvent);
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected action type: " + actionType);
        }
    }

    @Override
    public boolean isAvailable() {
        switch (actionType) {
            case DROP_OFF:
                return (userResourceWithType != null)
                        && (userResourceWithType.getResource().getAmount() > 0);
            case PICKUP:
                return (buildingResourceWithType != null)
                        && (buildingResourceWithType.getResource().getAmount() > 0);
            default:
                throw new IllegalStateException("Unexpected action type: " + actionType);
        }
    }
}
