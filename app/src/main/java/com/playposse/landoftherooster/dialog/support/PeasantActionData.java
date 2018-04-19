package com.playposse.landoftherooster.dialog.support;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import static com.playposse.landoftherooster.dialog.BuildingInteractionDialogFragment.IMPLIED_PEASANT_COUNT;

/**
 * An {@link ActionData} to assign a peasant to a building
 */
public class PeasantActionData extends ActionData {

    public static final int MAX_PEASANT_BUILDING_CAPACITY = 5;

    private final int unitTypeId;
    private final long buildingId;

    private int userUnitCount;
    private int buildingUnitCount;
    private UnitType unitType;

    public PeasantActionData(
            Context context,
            RoosterDao dao,
            int unitTypeId,
            long buildingId) {

        super(context, dao);

        this.unitTypeId = unitTypeId;
        this.buildingId = buildingId;

        // Load data.
        userUnitCount = dao.getUnitCountJoiningUser(unitTypeId);
        buildingUnitCount = dao.getUnitCount(unitTypeId, buildingId) + IMPLIED_PEASANT_COUNT;
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
    public boolean isAvailable() {
        return (userUnitCount > 0) && (buildingUnitCount < MAX_PEASANT_BUILDING_CAPACITY);
    }
}
