package com.playposse.landoftherooster.dialog;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;

/**
 * An {@link ActionData} for a resource.
 */
class ResourceActionData extends ActionData {

    private final int resourceTypeId;
    private final int buildingId;
    private final ActionType actionType;

    private ResourceWithType userResourceWithType;
    private ResourceWithType buildingResourceWithType;
    private ResourceType resourceType;

    ResourceActionData(
            Context context,
            RoosterDao dao,
            int resourceTypeId,
            int buildingId,
            ActionType ActionType) {

        super(context, dao);

        this.resourceTypeId = resourceTypeId;
        this.buildingId = buildingId;
        this.actionType = ActionType;

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
    protected String getUserString() {
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
    protected String getBuildingString() {
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
    protected String getActionString() {
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
                RoosterDaoUtil.moveResourceToBuilding(
                        getContext(),
                        userResourceWithType,
                        buildingResourceWithType,
                        resourceTypeId,
                        buildingId);
                break;
            case PICKUP:
                RoosterDaoUtil.moveResourceFromBuilding(
                        getContext(),
                        userResourceWithType,
                        buildingResourceWithType,
                        resourceTypeId,
                        buildingId);
            default:
                throw new IllegalStateException(
                        "Unexpected action type: " + actionType);
        }
    }

    @Override
    protected boolean isAvailable() {
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
