package com.playposse.landoftherooster.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.graphics.ColorUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import java.lang.ref.WeakReference;

/**
 * A class that keeps track of a marker on the Google Map. It is intelligent about detecting
 * changes and updating itself.
 * <p>
 * <p>TODO: Consider that completed resources cannot be picked up if the player has no carrying
 * capacity!
 */
public class MarkerState {

    private static final String LOG_TAG = MarkerState.class.getSimpleName();

    private static final String DRAWABLE_RESOURCE_TYPE = "drawable";

    private final Context context;
    private final RoosterDao dao;
    private final long buildingId;

    private boolean isReady;
    private int pendingProductionCount;
    private int completedProductionCount;
    private Marker marker;
    private Circle buildingZoneCircle;

    MarkerState(Context context, GoogleMap map, MapMarker mapMarker) {
        this.context = context;
        this.buildingId = mapMarker.getBuildingId();
        this.isReady = mapMarker.isReady();

        Integer pendingCount = mapMarker.getPendingProductionCount();
        this.pendingProductionCount = (pendingCount != null) ? pendingCount : 0;

        Integer completedCount = mapMarker.getCompletedProductionCount();
        this.completedProductionCount = (completedCount != null) ? completedCount : 0;

        dao = RoosterDatabase.getInstance(context).getDao();

        reapplyToMap(context, map);
    }

    public void refresh(Context context, GoogleMap map, MapMarker mapMarker) {
        int newPendingCount =
                (mapMarker.getPendingProductionCount() != null)
                        ? mapMarker.getPendingProductionCount() : 0;
        int newCompletedCount =
                (mapMarker.getCompletedProductionCount() != null)
                        ? mapMarker.getCompletedProductionCount() : 0;
        if ((mapMarker.isReady() == isReady)
                && (newPendingCount == this.pendingProductionCount)
                && (newCompletedCount == this.completedProductionCount)) {
            // Nothing has changed.
            return;
        }

        isReady = mapMarker.isReady();
        pendingProductionCount = newPendingCount;
        completedProductionCount = newCompletedCount;

        reapplyToMap(context, map);
    }

    private void reapplyToMap(Context context, GoogleMap map) {
        // Read data.
        // TODO: See if this database read can be avoided for performance.
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        Building building = buildingWithType.getBuilding();
        BuildingType buildingType = buildingWithType.getBuildingType();
        LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());

        // Generate building icon.
        Bitmap bitmap = drawBitmap(context, 100, 100, buildingType);
        Bitmap scaledBitmap =
                Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        BitmapDescriptor buildingIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

        // Apply marker to the map.
        new UpdateMapAsyncTask(this, map, latLng, buildingType.getName(), buildingIcon)
                .execute();
    }

    private Bitmap drawBitmap(Context context, int width, int height, BuildingType buildingType) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw ready background.
        if (isReady) {
            Drawable readyBgDrawable = GameConfig.BUILDING_READY_BG;
            readyBgDrawable.setBounds(0, 0, width, height);
            readyBgDrawable.draw(canvas);
        }

        // Draw building icon.
        int drawableId = context.getResources().getIdentifier(
                buildingType.getIcon(),
                DRAWABLE_RESOURCE_TYPE,
                context.getPackageName());
        Bitmap iconBitmap =
                BitmapFactory.decodeResource(context.getResources(), drawableId);
        Bitmap scaledBitmap =
                Bitmap.createScaledBitmap(iconBitmap, width, height, true);
        canvas.drawBitmap(scaledBitmap, 0, 0, null);

        // Draw pending resource circles.
        int circleIndex = 0;
        Paint pendingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pendingPaint.setColor(GameConfig.PENDING_PRODUCTION_COLOR);

        for (int i = 0; i < pendingProductionCount; i++) {
            if (circleIndex + completedProductionCount > GameConfig.MAX_PRODUCTION_CIRCLE_COUNT) {
                break;
            }

            drawProductionCircle(circleIndex, canvas, width, pendingPaint);
            circleIndex++;
        }

        // Draw completed resource circles.
        Paint completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(GameConfig.COMPLETED_PRODUCTION_COLOR);

        for (int i = 0; i < completedProductionCount; i++) {
            if (circleIndex > GameConfig.MAX_PRODUCTION_CIRCLE_COUNT) {
                break;
            }

            drawProductionCircle(circleIndex, canvas, width, completedPaint);
            circleIndex++;
        }

        return bitmap;
    }

    private void drawProductionCircle(int index, Canvas canvas, int width, Paint paint) {
        float margin = GameConfig.PRODUCTION_CIRCLE_MARGIN;
        float radius = GameConfig.PRODUCTION_CIRCLE_RADIUS;
        float x = width - margin - radius;
        x -= index * (2 * radius + margin);
        float y = margin + radius;

        canvas.drawCircle(x, y, radius, paint);
    }

    /**
     * An {@link AsyncTask} to update the map on the UI thread.
     */
    private static class UpdateMapAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<MarkerState> markerStateRef;
        private final GoogleMap map;
        private final LatLng position;
        private final String title;
        private final BitmapDescriptor buildingIcon;

        private UpdateMapAsyncTask(
                MarkerState markerState,
                GoogleMap map,
                LatLng position,
                String title,
                BitmapDescriptor buildingIcon) {

            this.map = map;
            this.position = position;
            this.title = title;
            this.buildingIcon = buildingIcon;

            markerStateRef = new WeakReference<>(markerState);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // The work is already done.
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MarkerState markerState = markerStateRef.get();

            if (markerState == null) {
                return;
            }

            if (markerState.marker == null) {
                markerState.marker = map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(title)
                        .icon(buildingIcon));

                createCircle(markerState);
            } else {
                markerState.marker.setIcon(buildingIcon);

                // Re-create circle.
                if (markerState.buildingZoneCircle != null) {
                    markerState.buildingZoneCircle.remove();
                    markerState.buildingZoneCircle = null;
                }
                createCircle(markerState);
            }
        }

        private void createCircle(MarkerState markerState) {
            int circleFillColor = (markerState.isReady)
                    ? GameConfig.BUILDING_READY_BG_COLOR
                    : GameConfig.BUILDING_DEFAULT_BG_COLOR;

            // Set the stroke color to half opacity to make it appear fuzzy. The location
            // detection on phones is inaccurate by a few feet.
            int circleStrokeColor = ColorUtils.setAlphaComponent(circleFillColor, 0xFF/2);

            markerState.buildingZoneCircle = map.addCircle(new CircleOptions()
                    .center(position)
                    .radius(GameConfig.INTERACTION_RADIUS)
                    .fillColor(circleFillColor)
                    .strokeColor(circleStrokeColor));
        }
    }
}
