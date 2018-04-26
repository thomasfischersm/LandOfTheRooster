package com.playposse.landoftherooster.services.time;

import android.content.Context;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A {@link SmartPeriodicService} that handles the healing on hospital type buildings.
 *
 * <p>TODO: Think about how to solve the problem that unit lists aren't stable. For example, the
 * service could wait for a unit with a lot of healing. The user could deposit another player with
 * little healing. The scheduled timer may be off.
 */
public class HospitalService extends SmartPeriodicService {

    public HospitalService(Context context) {
        super(context);

        addLiveData(getDao().getAllUnitsAsLiveData());

        reviewAllHospitals();
    }

    @Override
    protected void onLiveDataChanged() {
        reviewAllHospitals();
    }

    /**
     * Triggered by the expected healing period to have expired.
     */
    @Override
    protected void onBuildingEvent(BuildingWithType buildingWithType) {
        reviewHospital(buildingWithType);
    }

    private synchronized void reviewAllHospitals() {
        List<BuildingWithType> buildingWithTypes = getDao().getHealingBuildingsWithType();

        for (BuildingWithType buildingWithType : buildingWithTypes) {
            reviewHospital(buildingWithType);
        }
    }

    private void reviewHospital(BuildingWithType buildingWithType) {
        long buildingId = buildingWithType.getBuilding().getId();

        // Get units.
        List<UnitWithType> unitWithTypes = getDao().getUnitsWithTypeByBuildingId(buildingId);
        UnitWithType firstSickUnitWithType = getFirstSickUnit(unitWithTypes);
        boolean hasSickUnit = (firstSickUnitWithType != null);

        // Get times.
        Long healingFinishedMs =
                computeHealingFinished(buildingWithType, firstSickUnitWithType, unitWithTypes);
        long now = System.currentTimeMillis();
        boolean hasNoDate = (healingFinishedMs == null);
        boolean hasFinished = ((healingFinishedMs != null) && (healingFinishedMs < now));

        if (hasFinished) {
            if (hasSickUnit) {
                healNextUnit(buildingWithType, firstSickUnitWithType, unitWithTypes);
            } else {
                clearHealingStart(buildingWithType);
            }
        } else if (hasNoDate && hasSickUnit) {
            setHealingStart(buildingWithType, unitWithTypes);
        }
    }

    private void healNextUnit(
            BuildingWithType buildingWithType,
            UnitWithType firstSickUnitWithType,
            List<UnitWithType> unitWithTypes) {

        // Heal first sick unit.
        Unit firstSickUnit = firstSickUnitWithType.getUnit();
        UnitType firstSickUnitType = firstSickUnitWithType.getType();

        firstSickUnit.setHealth(firstSickUnitType.getHealth());
        getDao().update(firstSickUnit);

        // Check for next sick unit.
        UnitWithType secondSickUnitWithType = getFirstSickUnit(unitWithTypes);
        if (secondSickUnitWithType != null) {
            setHealingStart(buildingWithType, unitWithTypes);
        } else {
            clearHealingStart(buildingWithType);
        }
    }

    @Nullable
    private UnitWithType getFirstSickUnit(List<UnitWithType> unitWithTypes) {
        if (unitWithTypes != null) {
            for (UnitWithType unitWithType : unitWithTypes) {
                if (unitWithType.getUnit().getHealth() < unitWithType.getType().getHealth()) {
                    return unitWithType;
                }
            }
        }
        return null;
    }

    @Nullable
    private Long computeHealingFinished(
            BuildingWithType buildingWithType,
            UnitWithType firstSickUnitWithType,
            List<UnitWithType> unitWithTypes) {

        // Check if healing has started.
        Date healingStarted = buildingWithType.getBuilding().getHealingStarted();
        if (healingStarted == null) {
            return null;
        }

        // Compute needed health recovery.
        Unit unit = firstSickUnitWithType.getUnit();
        UnitType unitType = firstSickUnitWithType.getType();
        int neededHealth = unitType.getHealth() - unit.getHealth();

        int peasantCount = RoosterDaoUtil.getUnitAmount(GameConfig.PEASANT_ID, unitWithTypes)
                + GameConfig.IMPLIED_PEASANT_COUNT;

        // Calculate healing time.
        int healingRate = GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MINUTES;
        long healingTimeMs = neededHealth * healingRate * 60 * 1_000 / peasantCount;
        long healingStartedMs = healingStarted.getTime();
        return healingStartedMs + healingTimeMs;
    }

    private void clearHealingStart(BuildingWithType buildingWithType) {
        setHealingStart(buildingWithType, null, null);
    }

    private void setHealingStart(BuildingWithType buildingWithType, List<UnitWithType> unitWithTypes) {
        setHealingStart(buildingWithType, new Date(), unitWithTypes);
    }

    private void setHealingStart(
            BuildingWithType buildingWithType,
            @Nullable Date healingStarted,
            @Nullable List<UnitWithType> unitWithTypes) {

        // Update building.
        Building building = buildingWithType.getBuilding();
        building.setHealingStarted(healingStarted);
        getDao().update(building);

        // Schedule timer.
        if (unitWithTypes != null) {
            UnitWithType firstSickUnitWithType = getFirstSickUnit(unitWithTypes);
            if (firstSickUnitWithType != null) {
                Long healingFinished = computeHealingFinished(
                        buildingWithType,
                        firstSickUnitWithType,
                        unitWithTypes);

                if (healingFinished != null) {
                    long duration = healingFinished - System.currentTimeMillis();
                    scheduleNextBuildingEvent(buildingWithType, duration);
                }
            }
        }
    }
}
