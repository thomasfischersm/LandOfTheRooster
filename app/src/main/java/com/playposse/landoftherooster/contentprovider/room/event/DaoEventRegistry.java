package com.playposse.landoftherooster.contentprovider.room.event;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventObserver.EventType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A central place for all insert, update, and delete DAO calls to be reported. Observers can
 * listen to these change events.
 *
 * <p>LiveData allows observers to listen to changes to the whole table. This
 * {@link DaoEventRegistry} is smarter in that it reports which specific entity has changed. Thus
 * observers don't have to re-evaluate the whole table but can quickly check if the event is
 * even relevant because they know which entity has changed.
 */
public final class DaoEventRegistry {

    private final static DaoEventRegistry instance = new DaoEventRegistry();

    private final List<DaoEventObserver> observers = new ArrayList<>();

    private RoosterDao dao;

    private DaoEventRegistry() {
    }

    public static DaoEventRegistry get(RoosterDao dao) {
        if (instance.dao != dao) {
            instance.dao = dao;
        }

        return instance;
    }

    public static DaoEventRegistry get(Context context) {
        if (instance.dao == null) {
            instance.dao = RoosterDatabase.getInstance(context).getDao();
        }

        return instance;
    }

    public long insert(Building building) {
        long buildingId = dao.insert(building);
        building.setId(buildingId);

        for (DaoEventObserver observer : observers) {
            observer.onBuildingModified(building, EventType.INSERT);
        }

        return buildingId;
    }

    public void update(Building building) {
        dao.update(building);

        for (DaoEventObserver observer : observers) {
            observer.onBuildingModified(building, EventType.UPDATE);
        }
    }

    public long insert(Resource resource) {
        long resourceId = dao.insert(resource);
        resource.setId(resourceId);

        for (DaoEventObserver observer : observers) {
            observer.onResourceModified(resource, EventType.INSERT);
        }

        return resourceId;
    }

    public long insert(MapMarker mapMarker) {
        mapMarker.setLastModified(new Date());
        long id = dao.insert(mapMarker);
        mapMarker.setId(id);

        for (DaoEventObserver observer : observers) {
            observer.onMapMarkerModified(mapMarker, EventType.INSERT);
        }

        return id;
    }

    public void update(Resource resource) {
        dao.update(resource);

        for (DaoEventObserver observer : observers) {
            observer.onResourceModified(resource, EventType.UPDATE);
        }
    }

    public void updateLocation(
            Resource resource,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId) {

        if (!Objects.equals(resource.getLocatedAtBuildingId(), afterBuildingId)) {
            throw new IllegalArgumentException("The building ids should match: "
                    + resource.getLocatedAtBuildingId() + " != " + afterBuildingId);
        }

        // TODO: Think about if this should do the actual updating work.
        dao.update(resource);

        for (DaoEventObserver observer : observers) {
            observer.onResourceLocationUpdated(resource, beforeBuildingId, afterBuildingId);
        }
    }

    public void delete(Resource resource) {
        dao.delete(resource);

        for (DaoEventObserver observer : observers) {
            observer.onResourceModified(resource, EventType.DELETE);
        }
    }

    public long insert(Unit unit) {
        long unitId = dao.insert(unit);
        unit.setId(unitId);

        for (DaoEventObserver observer : observers) {
            observer.onUnitModified(unit, EventType.INSERT);
        }

        return unitId;
    }

    public void update(Unit unit) {
        dao.update(unit);

        for (DaoEventObserver observer : observers) {
            observer.onUnitModified(unit, EventType.UPDATE);
        }
    }

    public void updateLocation(
            Unit unit,
            @Nullable Long beforeBuildingId,
            @Nullable Long afterBuildingId) {

        if (!Objects.equals(unit.getLocatedAtBuildingId(), afterBuildingId)) {
            throw new IllegalArgumentException("The building ids should match: "
                    + unit.getLocatedAtBuildingId() + " != " + afterBuildingId);
        }

        // TODO: THink about if this should call the location setters.
        dao.update(unit);

        for (DaoEventObserver observer : observers) {
            observer.onUnitLocationUpdated(unit, beforeBuildingId, afterBuildingId);
        }
    }

    public void delete(Unit unit) {
        dao.delete(unit);

        for (DaoEventObserver observer : observers) {
            observer.onUnitModified(unit, EventType.DELETE);
        }
    }

    public void registerObserver(DaoEventObserver observer) {
        observers.add(observer);
    }

    public void registerObserver(DaoEventObserver observer, LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new RegisterThroughLifecycleObserver(observer));
    }

    public void unregisterObserver(DaoEventObserver observer) {
        observers.remove(observer);
    }

    /**
     * A {@link LifecycleObserver} that registers (on resume) and unregisters (on pause) a
     * {@link DaoEventObserver}.
     */
    class RegisterThroughLifecycleObserver implements LifecycleObserver {

        private final DaoEventObserver observer;

        private RegisterThroughLifecycleObserver(DaoEventObserver observer) {
            this.observer = observer;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void connectListener() {
            registerObserver(observer);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void disconnectListener() {
            unregisterObserver(observer);
        }
    }
}
