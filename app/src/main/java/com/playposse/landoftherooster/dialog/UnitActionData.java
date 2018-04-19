package com.playposse.landoftherooster.dialog;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

/**
 * An {@link ActionData} to assign a peasant to a building
 */
class UnitActionData extends ActionData {

    private final int unitTypeId;
    private final long buildingId;
    private final ActionType actionType;

    private int userUnitCount;
    private int buildingUnitCount;
    private UnitType unitType;

    UnitActionData(
            Context context,
            RoosterDao dao,
            int unitTypeId,
            long buildingId,
            ActionType actionType) {

        super(context, dao);

        this.unitTypeId = unitTypeId;
        this.buildingId = buildingId;
        this.actionType = actionType;

        // Load data.
        userUnitCount = dao.getUnitCountJoiningUser(unitTypeId);
        buildingUnitCount = dao.getUnitCount(unitTypeId, buildingId);
        unitType = dao.getUnitTypeById(unitTypeId);
    }

    @Override
    protected String getUserString() {
        String unitTypeName = unitType.getName();
        return getContext().getString(R.string.unit_template, userUnitCount, unitTypeName);
    }

    @Override
    protected String getBuildingString() {
        String unitTypeName = unitType.getName();
        return getContext().getString(R.string.unit_template, buildingUnitCount, unitTypeName);
    }

    @Override
    protected String getActionString() {
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
                RoosterDaoUtil.transferUnitToBuilding(
                        getContext(),
                        unitTypeId,
                        buildingId);
                break;
            case PICKUP:
                RoosterDaoUtil.transferUnitFromBuilding(
                        getContext(),
                        unitTypeId,
                        buildingId);
                break;
            default:
                throw new IllegalStateException("Unexpected action type: " + actionType);
        }
    }

    @Override
    protected boolean isAvailable() {
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
