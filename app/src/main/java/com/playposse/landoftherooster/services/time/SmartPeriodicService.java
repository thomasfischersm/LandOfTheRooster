package com.playposse.landoftherooster.services.time;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.util.AsyncObserver;
import com.playposse.landoftherooster.util.CancelableRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A background service that keeps updating things that happen to buildings. It's not actually
 * periodic. Rather than checking every minute, it intelligently knows when it should be triggered.
 */
public abstract class SmartPeriodicService implements LifecycleOwner {

    private final Context context;
    private final RoosterDao dao;
    private final LifecycleRegistry lifecycleRegistry;

    private ScheduledExecutorService scheduledExecutorService;

    private Map<Long, CancelableRunnable> buildingIdToRunnableMap = new HashMap<>();

    public SmartPeriodicService(Context context) {
        this.context = context;

        dao = RoosterDatabase.getInstance(context).getDao();

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    public void start() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    /**
     * Register a LiveData that will be observed and trigger updates.
     */
    protected <T> void addLiveData(LiveData<T> liveData) {
        AsyncObserver<SmartPeriodicService, T> observer =
                new AsyncObserver<SmartPeriodicService, T>(this) {
                    @Override
                    protected void onChangedAsync(SmartPeriodicService caller) {
                        onLiveDataChanged();
                    }
                };

        liveData.observe(this, observer);
    }

    protected abstract void onLiveDataChanged();

    protected void scheduleNextBuildingEvent(final BuildingWithType buildingWithType, long timeMs) {
        // Cancel potential previous building event.
        final long buildingId = buildingWithType.getBuilding().getId();
        if (buildingIdToRunnableMap.containsKey(buildingId)) {
            buildingIdToRunnableMap.get(buildingId).cancel();
        }

        CancelableRunnable runnable = new CancelableRunnable() {
            @Override
            public void maybeRun() {
                if (buildingIdToRunnableMap.containsKey(buildingId)) {
                    buildingIdToRunnableMap.remove(buildingId);
                }
                onBuildingEvent(buildingWithType);
            }
        };

        // Schedule Runnable.
        buildingIdToRunnableMap.put(buildingId, runnable);
        scheduledExecutorService.schedule(
                runnable,
                timeMs,
                TimeUnit.MILLISECONDS);
    }

    protected  abstract void onBuildingEvent(BuildingWithType buildingWithType);

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    public void stop() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);

        scheduledExecutorService.shutdownNow();
    }

    public Context getContext() {
        return context;
    }

    public RoosterDao getDao() {
        return dao;
    }
}
