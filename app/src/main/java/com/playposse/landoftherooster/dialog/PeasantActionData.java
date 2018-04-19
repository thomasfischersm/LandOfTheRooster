package com.playposse.landoftherooster.dialog;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import static com.playposse.landoftherooster.dialog.BuildingInteractionDialog.IMPLIED_PEASANT_COUNT;

/**
 * An {@link ActionData} to assign a peasant to a building
 */
class PeasantActionData extends ActionData {

    public static final int MAX_PEASANT_BUILDING_CAPACITY = 5;

    private final int unitTypeId;
    private final int buildingId;

    private int userUnitCount;
    private int buildingUnitCount;
    private UnitType unitType;

    PeasantActionData(
            Context context,
            RoosterDao dao,
            int unitTypeId,
            int buildingId) {

        super(context, dao);

        this.unitTypeId = unitTypeId;
        this.buildingId = buildingId;

        // Load data.
        userUnitCount = dao.getUnitCountJoiningUser(unitTypeId);
        buildingUnitCount = dao.getUnitCount(unitTypeId, buildingId) + IMPLIED_PEASANT_COUNT;
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
        return getContext().getString(R.string.assign_action);
    }

    @Override
    protected void performAction() {
        RoosterDaoUtil.transferUnitToBuilding(
                getContext(),
                unitTypeId,
                buildingId);
    }

    @Override
    protected boolean isAvailable() {
        return (userUnitCount > 0) && (buildingUnitCount < MAX_PEASANT_BUILDING_CAPACITY);
    }
}
