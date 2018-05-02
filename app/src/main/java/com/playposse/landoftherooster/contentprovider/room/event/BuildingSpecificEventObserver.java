package com.playposse.landoftherooster.contentprovider.room.event;

import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A convenience {@link DaoEventObserver} that only listens to events that affect a particular
 * building. The events include resources/units changing location to/from that building.
 */
public abstract class BuildingSpecificEventObserver implements DaoEventObserver {

    private final long specifiedBuildingId;

    public BuildingSpecificEventObserver(long specifiedBuildingId) {
        this.specifiedBuildingId = specifiedBuildingId;
    }

    @Override
    public void onBuildingModified(Building building, EventType eventType) {
        if (specifiedBuildingId == building.getId()) {
            onRelevantBuildingUpdate(building.getId());
        }
    }

    @Override
    public void onResourceModified(Resource resource, EventType eventType) {
        if ((eventType == EventType.INSERT)
                && Objects.equals(specifiedBuildingId, resource.getLocatedAtBuildingId())) {
            onRelevantBuildingUpdate(specifiedBuildingId);
        }
    }

    @Override
    public void onResourceLocationUpdated(
            Resource resource,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId) {

        if ((beforeBuildingId != null) && (specifiedBuildingId == beforeBuildingId)) {
            onRelevantBuildingUpdate(beforeBuildingId);
        } else if ((afterBuildingId != null) && (specifiedBuildingId == afterBuildingId)) {
            onRelevantBuildingUpdate(afterBuildingId);
        }
    }

    @Override
    public void onUnitModified(Unit unit, EventType eventType) {
        if ((eventType == EventType.INSERT)
                && Objects.equals(specifiedBuildingId, unit.getLocatedAtBuildingId())) {
            onRelevantBuildingUpdate(specifiedBuildingId);
        }
    }

    @Override
    public void onUnitLocationUpdated(
            Unit unit,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId) {

        if ((beforeBuildingId != null) && (specifiedBuildingId == beforeBuildingId)) {
            onRelevantBuildingUpdate(beforeBuildingId);
        } else if ((afterBuildingId != null) && (specifiedBuildingId == afterBuildingId)) {
            onRelevantBuildingUpdate(afterBuildingId);
        }
    }

    protected abstract void onRelevantBuildingUpdate(long buildingId);
}
