package com.playposse.landoftherooster.dialog.support;

import android.content.Context;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import static com.playposse.landoftherooster.GameConfig.IMPLIED_PEASANT_COUNT;
import static com.playposse.landoftherooster.GameConfig.MAX_PEASANT_BUILDING_CAPACITY;

/**
 * An {@link ActionData} to assign a peasant to a building
 */
public class PeasantActionData extends ActionData {

    private final long unitTypeId;
    private final long buildingId;

    private int userUnitCount;
    private int buildingUnitCount;
    private UnitType unitType;

    public PeasantActionData(
            Context context,
            RoosterDao dao,
            long unitTypeId,
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
        AssignPeasantEvent event = new AssignPeasantEvent(buildingId);
        BusinessEngine.get()
                .triggerEvent(event);
    }

    @Override
    public boolean isAvailable() {
        return (userUnitCount > 0) && (buildingUnitCount < MAX_PEASANT_BUILDING_CAPACITY);
    }
}
