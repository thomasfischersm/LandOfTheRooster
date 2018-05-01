package com.playposse.landoftherooster.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.util.TimedAsyncTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A collection of Google map markers and methods to manage them.
 */
public class MarkerStateRegistry {

    private final Context context;
    private final GoogleMap map;

    private Map<Long, MarkerState> markerStates = new HashMap<>();

    public MarkerStateRegistry(FragmentActivity activity, GoogleMap map) {
        this.context = activity.getApplicationContext();
        this.map = map;

        // Observe new buildings being added.
        Observer<List<BuildingWithType>> buildingObserver = new Observer<List<BuildingWithType>>() {
            @Override
            public void onChanged(@Nullable final List<BuildingWithType> buildingsWithType) {
                new BuildingUpdateAsyncTask(
                        MarkerStateRegistry.this,
                        buildingsWithType)
                        .execute();
            }
        };
        LiveData<List<BuildingWithType>> buildingLiveData =
                RoosterDatabase.getInstance(activity).getDao().getAllBuildingsWithTypeAsLiveData();
        buildingLiveData.observe(activity, buildingObserver);

        // Observe resource changes.
        Observer<List<Resource>> resourceObserver = new Observer<List<Resource>>() {
            @Override
            public void onChanged(@Nullable List<Resource> resources) {
                new ResourceUpdateAsyncTask(
                        MarkerStateRegistry.this,
                        resources)
                        .execute();
            }
        };
        LiveData<List<Resource>> resourceLiveData =
                RoosterDatabase.getInstance(activity).getDao().getAllResourcesAsLiveData();
        resourceLiveData.observe(activity, resourceObserver);

        // Observe resource changes.
        Observer<List<Unit>> unitObserver = new Observer<List<Unit>>() {
            @Override
            public void onChanged(@Nullable List<Unit> units) {
                new UnitUpdateAsyncTask(
                        MarkerStateRegistry.this,
                        units)
                        .execute();
            }
        };
        LiveData<List<Unit>> unitLiveData =
                RoosterDatabase.getInstance(activity).getDao().getAllUnitsAsLiveData();
        unitLiveData.observe(activity, unitObserver);
    }

    private void addBuilding(Context context, GoogleMap map, BuildingWithType buildingWithType) {
        long buildingId = buildingWithType.getBuilding().getId();
        if (markerStates.containsKey(buildingId)) {
            // Already added. Skip.
            return;
        }

        MarkerState markerState = new MarkerState(context, buildingId);
        markerStates.put(buildingId, markerState);
        markerState.checkForChange(map);
    }

    private void checkForChange() {
        for (MarkerState markerState : markerStates.values()) {
            markerState.checkForChange(map);
        }
    }

    private void onBuildingUpdate(List<BuildingWithType> buildingsWithType) {
        for (BuildingWithType buildingWithType : buildingsWithType) {
            addBuilding(context, map, buildingWithType);
        }
    }

    private void onResourceUpdate(List<Resource> resources) {
        checkForChange();
    }

    private void onUnitUpdate(List<Unit> units) {
        checkForChange();
    }

    /**
     * Awkward {@link AsyncTask} pattern to get the database work on a background thread.
     */
    private static class BuildingUpdateAsyncTask extends TimedAsyncTask {

        private final WeakReference<MarkerStateRegistry> markerStateRegistryRef;
        private final List<BuildingWithType> buildingsWithType;

        private BuildingUpdateAsyncTask(
                MarkerStateRegistry markerStateRegistry,
                List<BuildingWithType> buildingsWithType) {

            this.buildingsWithType = buildingsWithType;

            markerStateRegistryRef = new WeakReference<>(markerStateRegistry);
        }

        @Override
        protected void doInBackground() {
            MarkerStateRegistry markerStateRegistry = markerStateRegistryRef.get();

            if (markerStateRegistry != null) {
                markerStateRegistry.onBuildingUpdate(buildingsWithType);
            }
        }
    }

    /**
     * Awkward {@link AsyncTask} pattern to get the database work on a background thread.
     */
    private static class ResourceUpdateAsyncTask extends TimedAsyncTask {

        private final WeakReference<MarkerStateRegistry> markerStateRegistryRef;
        private final List<Resource> resources;

        private ResourceUpdateAsyncTask(
                MarkerStateRegistry markerStateRegistry,
                List<Resource> resources) {

            this.resources = resources;

            markerStateRegistryRef = new WeakReference<>(markerStateRegistry);
        }

        @Override
        protected void doInBackground() {
            MarkerStateRegistry markerStateRegistry = markerStateRegistryRef.get();

            if (markerStateRegistry != null) {
                markerStateRegistry.onResourceUpdate(resources);
            }
        }
    }

    /**
     * Awkward {@link AsyncTask} pattern to get the database work on a background thread.
     */
    private static class UnitUpdateAsyncTask extends TimedAsyncTask {

        private final WeakReference<MarkerStateRegistry> markerStateRegistryRef;
        private final List<Unit> units;

        private UnitUpdateAsyncTask(
                MarkerStateRegistry markerStateRegistry,
                List<Unit> units) {

            this.units = units;

            markerStateRegistryRef = new WeakReference<>(markerStateRegistry);
        }

        @Override
        protected void doInBackground() {
            MarkerStateRegistry markerStateRegistry = markerStateRegistryRef.get();

            if (markerStateRegistry != null) {
                markerStateRegistry.onUnitUpdate(units);
            }
        }
    }
}
