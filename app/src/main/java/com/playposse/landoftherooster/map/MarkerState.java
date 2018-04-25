package com.playposse.landoftherooster.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * A class that keeps track of a marker on the Google Map. It is intelligent about detecting
 * changes and updating itself.
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

    MarkerState(Context context, long buildingId) {
        this.context = context;
        this.buildingId = buildingId;

        dao = RoosterDatabase.getInstance(context).getDao();
    }

    public void checkForChange(GoogleMap map) {
        long start = System.currentTimeMillis();

        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        Building building = buildingWithType.getBuilding();
        List<ProductionRule> productionRules =
                dao.getProductionRulesByBuildingTypeId(building.getBuildingTypeId());
        Map<Long, Integer> resourceMap = ProductionCycleUtil.getResourcesInBuilding(dao, building);
        Map<Long, Integer> unitMap = ProductionCycleUtil.getUnitCountsInBuilding(dao, building);

        int newPendingCount = computePendingProductionCount(productionRules, resourceMap, unitMap);
        int newCompletedCount =
                computeCompletedProductionCount(productionRules, resourceMap, unitMap);
        boolean newIsReady = (newCompletedCount > 0);

        if ((pendingProductionCount == newPendingCount)
                && (completedProductionCount == newCompletedCount)
                && (isReady == newIsReady)
                && (marker != null)) {
            // Nothing to do. There is no change.
            return;
        }

        isReady = newIsReady;
        pendingProductionCount = newPendingCount;
        completedProductionCount = newCompletedCount;
        reapplyToMap(context, map);

        long end = System.currentTimeMillis();
        Log.i(LOG_TAG, "checkForChange: Checked building for change: "
                + buildingWithType.getBuildingType().getName() + " "
                + (end - start)
                + "ms. isReady: " + isReady
                + " pendingProductionCount: " + pendingProductionCount
                + " completedProductionCount " + completedProductionCount);
    }

    private int computePendingProductionCount(
            List<ProductionRule> productionRules,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        int totalCount = 0;

        for (ProductionRule productionRule : productionRules) {
            Integer count = null;

            // Get max possible production count with available resources.
            for (long resourceTypeId : productionRule.getSplitInputResourceTypeIds()) {
                if (resourceMap.containsKey(resourceTypeId)) {
                    if (count == null) {
                        count = resourceMap.get(resourceTypeId);
                    } else {
                        count = Math.max(count, resourceMap.get(resourceTypeId));
                    }
                } else {
                    count = 0;
                }
            }

            // Get max possible production count with available units.
            for (long unitTypeId : productionRule.getSplitInputUnitTypeIds()) {
                if (unitMap.containsKey(unitTypeId)) {
                    if (count == null) {
                        count = unitMap.get(unitTypeId);
                    } else {
                        count = Math.max(count, unitMap.get(unitTypeId));
                    }
                } else {
                    count = 0;
                }
            }

            if (count != null) {
                totalCount += count;
            } else {
                // This is a free production rule.
                totalCount++;
            }
        }

        return totalCount;
    }

    private int computeCompletedProductionCount(
            List<ProductionRule> productionRules,
            Map<Long, Integer> resourceMap,
            Map<Long, Integer> unitMap) {

        int totalCount = 0;

        for (ProductionRule productionRule : productionRules) {
            Long resourceTypeId = productionRule.getOutputResourceTypeId();
            if (resourceTypeId != null) {
                if (resourceMap.containsKey(resourceTypeId)) {
                    totalCount += resourceMap.get(resourceTypeId);
                }
            }

            Long unitTypeId = productionRule.getOutputUnitTypeId();
            if (unitTypeId != null) {
                if (unitMap.containsKey(unitTypeId)) {
                    totalCount += unitMap.get(unitTypeId);
                }
            }
        }

        return totalCount;
    }

    private void reapplyToMap(Context context, GoogleMap map) {
        // Read data.
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
        int margin = GameConfig.PRODUCTION_CIRCLE_MARGIN;
        int radius = GameConfig.PRODUCTION_CIRCLE_RADIUS;
        int x = width - margin - radius;
        x += index * (2 * radius + margin);
        int y = margin + radius;

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
                ;
            } else {
                markerState.marker.setIcon(buildingIcon);
            }
        }
    }
}
