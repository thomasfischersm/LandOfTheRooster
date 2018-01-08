package com.playposse.landoftherooster.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.KingdomActivity;
import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.Building;
import com.playposse.landoftherooster.contentprovider.room.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.ConvenientLocationProvider;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * A background service that discovers buildings.
 * <p>
 * <p>All buildings are discovered in sequence. A building is discovered when the user is at a
 * specific distance from the last discovered building. The specific distance is a randomly chosen
 * distance within the min/max range for that building type.
 */
public class BuildingDiscoveryService extends Service {

    private static final String LOG_TAG = BuildingDiscoveryService.class.getSimpleName();

    private static final int INITIAL_BUILDING_TYPE = 0;
    private static final int LOCATION_CHECK_INTERVAL = 1_000;
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "com.playposse.landoftherooster.notificationchannel";
    private static final String NOTIFICATION_CHANNEL_NAME = "Land Of The Rooster";

    /**
     * An additional distance that the user can go while still being able to discover a building.
     *
     * <p>Each building type has a min and a max. The discovery service decides on an actual
     * distance somewhere between those maximum. As long as the user has walked the distance, a
     * discovery happens. However, to prevent a really far building from all other buildings, a
     * safeguard prevents the discovery when the user exceeds the max distance.
     *
     * <p>While that is good, if the actual distance and the max distance is very close, GPS
     * inaccuracy could make it hard to for the user to trigger. So a fudge factor is added to max
     * to ensure that a reasonable discovery is made.
     */
    private static final int MAX_GRACE_DISTANCE = 100;

    private final Random random = new Random();

    private ConvenientLocationProvider convenientLocationProvider;
    private BuildingType nextBuildingType;
    private Integer nextDistance;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundForOreo();
        } else {
            startForegroundPriorOreo();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void init() {
        initNextBuildingType();

        try {
            convenientLocationProvider = new ConvenientLocationProvider(
                    getApplicationContext(),
                    LOCATION_CHECK_INTERVAL,
                    new LocationCallback());
        } catch (ExecutionException | InterruptedException ex) {
            Log.e(LOG_TAG, "BuildingDiscoveryService: Failed to wait for permissions.", ex);
        }
    }

    @Override
    public void onDestroy() {
        convenientLocationProvider.close();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startForegroundForOreo() {
        Context context = getApplicationContext();

        // Create notification channel.
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create pending intent to open Kingdom activity.
        Intent notificationIntent = new Intent(context, KingdomActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // Create notification for foreground service.
        Notification notification =
                new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getText(R.string.service_notification_title))
                        .setContentText(getText(R.string.service_notification_msg))
                        .setSmallIcon(R.drawable.rooster)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_msg))
                        .build();

        // Move the service into the foreground.
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private void startForegroundPriorOreo() {
        Context context = getApplicationContext();

        // Create pending intent to open Kingdom activity.
        Intent notificationIntent = new Intent(context, KingdomActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // Create notification for foreground service.
        Notification notification =
                new Notification.Builder(context)
                        .setContentTitle(getText(R.string.service_notification_title))
                        .setContentText(getText(R.string.service_notification_msg))
                        .setSmallIcon(R.drawable.rooster)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_msg))
                        .build();

        // Move the service into the foreground.
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private void initNextBuildingType() {
        Context context = getApplicationContext();
        Building lastBuilding = getLastBuilding(context);

        if (lastBuilding == null) {
            nextBuildingType = getNextBuildingType(context, INITIAL_BUILDING_TYPE);
        } else {
            nextBuildingType = getNextBuildingType(context, lastBuilding.getBuildingTypeId());
        }

        if ((nextBuildingType != null) && (nextBuildingType.getMinDistanceMeters() != null)) {
            Log.d(LOG_TAG, "initNextBuildingType: The next building type is: "
                    + nextBuildingType.getName());
            int min = nextBuildingType.getMinDistanceMeters();
            int delta = nextBuildingType.getMaxDistanceMeters()
                    - nextBuildingType.getMinDistanceMeters();
            nextDistance = min + random.nextInt(delta);
            Log.d(LOG_TAG, "initNextBuildingType: Distance to the next building is "
                    + nextDistance);
        } else {
            Log.d(LOG_TAG, "initNextBuildingType: There are no more buildings to be " +
                    "discovered.");
            nextDistance = null;
        }
    }

    @Nullable
    private static BuildingType getNextBuildingType(Context context, int lastBuildingTypeId) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getNextBuildingType(lastBuildingTypeId);
    }

    @Nullable
    private static Building getLastBuilding(Context context) {
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        return dao.getLastBuilding();
    }

    private void handleFirstBuilding(LatLng latLng) {
        if ((nextBuildingType == null) || (nextBuildingType.getMinDistanceMeters() != null)) {
            return;
        }

        placeNextBuilding(latLng);
    }

    private void placeNextBuilding(LatLng currentLatLng) {
        // Check if we have a good GPS location.

        // Create the building.
        Building building =
                new Building(nextBuildingType.getId(), currentLatLng.latitude, currentLatLng.longitude);
        Context context = getApplicationContext();
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        dao.insertBuilding(building);
        Log.d(LOG_TAG, "placeNextBuilding: Placed building: " + nextBuildingType.getName());

        // Prepare to place the next building.
        initNextBuildingType();

        // TODO: This will have to notify the activity to do something.
        // Maybe, the activity can simply observe the building table?
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void checkIfBuildingDiscovered(LatLng currentLatLng) {
        if (nextBuildingType == null) {
            Log.d(LOG_TAG, "checkIfBuildingDiscovered: No more next building types to " +
                    "discover.");
            return;
        }

        Float distance = getMinDistanceFromCurrentBuildings(currentLatLng);

        if (distance == null) {
            Log.e(LOG_TAG, "checkIfBuildingDiscovered: Can't check because the distance is " +
                    "null!");
            return;
        }

        Integer limit = nextBuildingType.getMaxDistanceMeters() + MAX_GRACE_DISTANCE;
        if ((distance > nextDistance) && (distance < limit)) {
            Log.d(LOG_TAG, "checkIfBuildingDiscovered: Discovered the next building: "
                    + nextBuildingType.getName());
            placeNextBuilding(currentLatLng);
        }
    }

    @Nullable
    private Float getMinDistanceFromCurrentBuildings(LatLng currentLatLng) {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatLng.latitude);
        currentLocation.setLongitude(currentLatLng.longitude);

        Float min = null;
        Float max = null; // Gather for future use.

        // Query db.
        RoosterDao dao = RoosterDatabase.getInstance(getApplicationContext()).getDao();
        List<Building> buildings = dao.getAllBuildings();

        // Iterate over buildings and collect min/max
        for (Building building : buildings) {
            Location buildingLocation = new Location("");
            buildingLocation.setLatitude(building.getLatitude());
            buildingLocation.setLongitude(building.getLongitude());

            float distance = currentLocation.distanceTo(buildingLocation);

            if ((min == null) || (max == null)) {
                min = distance;
                max = distance;
            } else {
                min = Math.min(min, distance);
                max = Math.max(max, distance);
            }
        }

        Log.d(LOG_TAG, "getMinDistanceFromCurrentBuildings: Min distance from buildings: "
                + min);
        return min;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Callback that gets called when a new GPS location becomes available. At that time, new
     * buildings are created if the user is ready.
     */
    private class LocationCallback implements ConvenientLocationProvider.Callback {

        @Override
        public void onNewLocation(LatLng latLng) {
            handleFirstBuilding(latLng);
            checkIfBuildingDiscovered(latLng);
        }
    }
}
