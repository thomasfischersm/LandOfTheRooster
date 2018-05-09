package com.playposse.landoftherooster.contentprovider.room.event;

import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;

import javax.annotation.Nullable;

/**
 * An observer that listens to updates to the database through the {@link DaoEventRegistry}.
 */
public interface DaoEventObserver {

    enum EventType {
        INSERT,
        UPDATE,
        DELETE
    }

    void onBuildingModified(Building building, EventType eventType);

    void onResourceModified(Resource resource, EventType eventType);

    void onMapMarkerModified(MapMarker mapMarker, EventType eventType);

    void onResourceLocationUpdated(
            Resource resource,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId);

    void onUnitModified(Unit unit, EventType eventType);

    void onUnitLocationUpdated(
            Unit unit,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId);
}
