package com.playposse.landoftherooster.services.broadcastintent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A more typed version of {@link LocalBroadcastManager} that's specific for events of this app.
 */
public class RoosterBroadcastManager {

    private static final String LOG_TAG = RoosterBroadcastManager.class.getSimpleName();

    private static RoosterBroadcastManager instance;

    private final Context context;

    private LocalBroadcastReceiver localReceiver;
    private List<RoosterBroadcastReceiver> roosterReceivers = new ArrayList<>();

    private List<Class<? extends RoosterBroadcastIntent>> roosterIntentRegistry =
            new ArrayList<Class<? extends RoosterBroadcastIntent>>() {{
                add(BattleAvailableBroadcastIntent.class);
                add(LeftBuildingBroadcastIntent.class);
                add(BuildingNeedsToRespawnBroadcastIntent.class);
                add(BuildingAvailableBroadcastIntent.class);
            }};

    public RoosterBroadcastManager(Context context) {
        this.context = context;
    }

    public static RoosterBroadcastManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoosterBroadcastManager(context);
        }

        return instance;
    }

    public void register(RoosterBroadcastReceiver roosterReceiver) {
        roosterReceivers.add(roosterReceiver);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        // Register local receiver if necessary.
        if (localReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            String packageName = getClass().getPackage().getName();
            for (Class<? extends RoosterBroadcastIntent> roosterIntent : roosterIntentRegistry) {
                intentFilter.addAction(packageName + "." + roosterIntent.getSimpleName());
            }

            localReceiver = new LocalBroadcastReceiver();
            localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        }

        IntentFilter intentFilter = new IntentFilter();
        String packageName = getClass().getPackage().getName();
        for (Class<? extends RoosterBroadcastIntent> roosterIntent : roosterIntentRegistry) {
            intentFilter.addAction(roosterIntent.getName());
        }
    }

    public static void send(Context context, RoosterBroadcastIntent roosterIntent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(roosterIntent.createLocalIntent());
    }

    public void unregister(RoosterBroadcastReceiver roosterReceiver) {
        roosterReceivers.remove(roosterReceiver);

        if (roosterReceivers.size() == 0) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }

    /**
     * Implementation of {@link BroadcastReceiver} that casts intents to their target intent.
     */
    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Check for registered rooster intent.
            try {
                for (Class<? extends RoosterBroadcastIntent> roosterIntentClass : roosterIntentRegistry) {
                    if (roosterIntentClass.getName().equals(action)) {
                        Constructor<? extends RoosterBroadcastIntent> constructor =
                                roosterIntentClass.getConstructor();
                        RoosterBroadcastIntent roosterIntent = constructor.newInstance();
                        roosterIntent.createFromIntent(intent);

                        for (RoosterBroadcastReceiver roosterReceiver : roosterReceivers) {
                            roosterReceiver.onReceive(roosterIntent);
                        }
                        return;
                    }
                }
            } catch (NoSuchMethodException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException ex) {
                Log.e(LOG_TAG, "onReceive: Failed to instantiate RoosterBroadcastIntent.", ex);
            }
        }
    }

    /**
     * An interface for interested activities to receive events of type
     * {@link RoosterBroadcastIntent}.
     */
    public interface RoosterBroadcastReceiver {

        void onReceive(RoosterBroadcastIntent roosterIntent);
    }
}
