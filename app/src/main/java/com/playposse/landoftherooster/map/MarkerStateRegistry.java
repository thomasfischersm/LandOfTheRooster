package com.playposse.landoftherooster.map;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.UserTapsBuildingMarkerEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventObserver;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A collection of Google map markers and methods to manage them.
 */
public class MarkerStateRegistry {

    private final Context context;
    private final GoogleMap map;
    private final RoosterDao dao;

    private Map<Long, MarkerState> markerStates = new HashMap<>();
    private MapMarkerObserver mapMarkerObserver;

    public MarkerStateRegistry(FragmentActivity activity, GoogleMap map) {
        this.context = activity.getApplicationContext();
        this.map = map;

        dao = RoosterDatabase.getInstance(context).getDao();
    }

    public void start() {
        // Reset the map.
        map.clear();

        new AddAllMapMarkersAsyncTask().execute();

        map.setOnMarkerClickListener(new ShowBuildingDialogListener());
    }

    public void stop() {
        if (mapMarkerObserver != null) {
            DaoEventRegistry.get(dao)
                    .unregisterObserver(mapMarkerObserver);
            mapMarkerObserver = null;
        }

        map.setOnMarkerClickListener(null);
    }

    @Nullable
    private MarkerState getMarkerStateByMarker(Marker marker) {
        for (MarkerState markerState : markerStates.values()) {
            if (Objects.equals(markerState.getMarker().getId(), marker.getId())) {
                return markerState;
            }
        }
        return null;
    }

    /**
     * An {@link AsyncTask} that loads all the {@link MapMarker}s for the first time.
     */
    private class AddAllMapMarkersAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            List<MapMarker> mapMarkers = dao.getAllMapMarkers();

            for (MapMarker mapMarker : mapMarkers) {
                MarkerState markerState = new MarkerState(context, map, mapMarker);
                markerStates.put(mapMarker.getBuildingId(), markerState);
            }

            // Listen to MapMarker updates.
            mapMarkerObserver = new MapMarkerObserver();
            DaoEventRegistry.get(dao)
                    .registerObserver(mapMarkerObserver);

            return null;
        }
    }

    /**
     * A {@link DaoEventObserver} that listens to only {@link MapMarker} inserts and updates.
     */
    private class MapMarkerObserver implements DaoEventObserver {

        @Override
        public void onMapMarkerModified(MapMarker mapMarker, EventType eventType) {
            if (eventType == EventType.INSERT) {
                MarkerState markerState = new MarkerState(context, map, mapMarker);
                markerStates.put(mapMarker.getBuildingId(), markerState);
            } else if (eventType == EventType.UPDATE) {
                MarkerState markerState = markerStates.get(mapMarker.getBuildingId());
                markerState.refresh(context, map, mapMarker);
            }
        }

        @Override
        public void onBuildingModified(Building building, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onResourceModified(Resource resource, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onResourceLocationUpdated(
                Resource resource,
                @Nullable Long beforeBuildingId,
                @Nullable Long afterBuildingId) {
            // Ignore.
        }

        @Override
        public void onUnitModified(Unit unit, EventType eventType) {
            // Ignore.
        }

        @Override
        public void onUnitLocationUpdated(
                Unit unit, @Nullable Long beforeBuildingId,
                @Nullable Long afterBuildingId) {
            // Ignore.
        }
    }

    /**
     * A {@link OnMarkerClickListener} that opens dialogs for buildings.
     */
    private class ShowBuildingDialogListener implements OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {
            MarkerState markerState = getMarkerStateByMarker(marker);

            if (markerState != null) {
                new ShowDialogAsyncTask(markerState).execute();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * An {@link AsyncTask} that opens a building dialog.
     */
    private static class ShowDialogAsyncTask extends AsyncTask<Void, Void, Void> {

        private final MarkerState markerState;

        private ShowDialogAsyncTask(MarkerState markerState) {
            this.markerState = markerState;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long buildingId = markerState.getBuildingId();
            BusinessEngine.get()
                    .triggerEvent(new UserTapsBuildingMarkerEvent(buildingId));

            return null;
        }
    }
}
