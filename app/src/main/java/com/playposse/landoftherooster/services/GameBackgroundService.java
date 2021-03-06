package com.playposse.landoftherooster.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.activity.KingdomActivity;
import com.playposse.landoftherooster.activity.StopActivity;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingRepository;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.LocationUpdateEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.ConvenientLocationProvider;

import java.util.concurrent.ExecutionException;

/**
 * A service that monitors the location and calls child services to handle events around the
 * users movement through the physical world.
 */
public class GameBackgroundService extends Service {

    private static final String LOG_TAG = GameBackgroundService.class.getSimpleName();

    private static final int LOCATION_CHECK_INTERVAL = 1_000;
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "com.playposse.landoftherooster.notificationchannel";
    private static final String NOTIFICATION_CHANNEL_NAME = "Land Of The Rooster";

    private static boolean isLocationOverride = false;

    private ConvenientLocationProvider convenientLocationProvider;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if already started.
        if (convenientLocationProvider != null) {
            return START_NOT_STICKY;
        }

        // Enter foreground.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundForOreo();
        } else {
            startForegroundPriorOreo();
        }

        // Start location scanning.
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }).start();

        return START_NOT_STICKY;
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

        // Create pending action to stop the service.
        Intent stopIntent = new Intent(context, StopActivity.class);
        PendingIntent pendingStopIntent =
                PendingIntent.getActivity(context, 0, stopIntent, 0);
        Notification.Action stopAction = new Notification.Action.Builder(
                R.drawable.ic_stop_black_24dp,
                getString(R.string.service_notification_stop_action),
                pendingStopIntent)
                .build();

        // Create notification for foreground service.
        Notification notification =
                new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getText(R.string.service_notification_title))
                        .setContentText(getText(R.string.service_notification_msg))
                        .setSmallIcon(R.drawable.rooster)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_msg))
                        .addAction(stopAction)
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

        // Create pending action to stop the service.
        Intent stopIntent = new Intent(context, StopActivity.class);
        PendingIntent pendingStopIntent =
                PendingIntent.getActivity(context, 0, stopIntent, 0);

        // Create notification for foreground service.
        Notification notification =
                new Notification.Builder(context)
                        .setContentTitle(getText(R.string.service_notification_title))
                        .setContentText(getText(R.string.service_notification_msg))
                        .setSmallIcon(R.drawable.rooster)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_msg))
                        .addAction(R.drawable.ic_stop_black_24dp,
                                getString(R.string.service_notification_stop_action),
                                pendingStopIntent)
                        .build();

        // Move the service into the foreground.
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private void init() {
        // Force the repository to initialize.
        RoosterDao dao = RoosterDatabase.getInstance(this).getDao();
        BuildingRepository.get(dao);

        BusinessEngine.get()
                .start(this);

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
        if (convenientLocationProvider != null) {
            convenientLocationProvider.close();
            convenientLocationProvider = null;
        }

        BusinessEngine.get()
                .stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isLocationOverride() {
        return isLocationOverride;
    }

    public static void setLocationOverride(boolean locationOverride) {
        isLocationOverride = locationOverride;
    }

    public static void setOverrideLocation(LatLng overrideLocation) {
        BusinessEngine.get()
                .triggerEventAsync(new LocationUpdateEvent(overrideLocation));
    }

    /**
     * Callback that gets called when a new GPS location becomes available. This calls all
     * dependent services.
     */
    private class LocationCallback implements ConvenientLocationProvider.Callback {

        @Override
        public void onNewLocation(LatLng latLng) {
            if (!isLocationOverride) {
                BusinessEngine.get()
                        .triggerEventAsync(new LocationUpdateEvent(latLng));
            }
        }
    }
}
