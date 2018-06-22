package com.playposse.landoftherooster.analytics;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A util to log analytics to Firebase, Fabric, and Google Analytics.
 */
public final class Analytics {

    public enum AppEvent {
        DISCOVER_BUILDING,
        ADMIT_UNIT_TO_HOSPITAL,
        ASSIGN_PEASANT,
        DROP_OFF_ITEM,
        EXECUTE_BATTLE,
        PICK_UP_ITEM,
        PICK_UP_UNIT_FROM_HOSPITAL,
        SHOW_BATTLE_RESPAWN_DIALOG,
        SHOW_HEALING_BUILDING_DIALOG,
        SHOW_PRODUCTION_BUILDING_DIALOG,
        SHOW_BATTLE_BUILDING_DIALOG
    }

    private enum AnalyticsEvent {
        BUSINESS_EVENT
    }

    public static final String EVENT_NAME_ATTRIBUTE = "eventName";
    public static final String PRECONDITION_SUCCESS_ATTRIBUTE = "preconditionSuccess";
    public static final String PRECONDITION_FAILURE_ATTRIBUTE = "preconditionFailure";

    private static final String DURATION_MS_ATTRIBUTE = "durationMs";

    private static Context appContext;

    private Analytics() {
    }

    public static void logBusinessEvent(BusinessEvent event, long start) {
        long end = System.currentTimeMillis();
        String eventName = event.getClass().getSimpleName();

        // Report to Firebase.
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(appContext);
        analytics.logEvent(eventName, null);

        // Report to Fabric Answer.
        Answers.getInstance().logCustom(new CustomEvent(eventName)
                .putCustomAttribute(EVENT_NAME_ATTRIBUTE, eventName)
                .putCustomAttribute(DURATION_MS_ATTRIBUTE, end - start));
    }

    public static void init(Context appContext) {
        Analytics.appContext = appContext;
    }

    public static void reportEvent(AppEvent appEvent) {
        if (appContext == null) {
            // not yet initialized
            return;
        }

        // Report to Firebase analytics.
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(appContext);
        analytics.logEvent(appEvent.name(), null);

        // Report to Fabric Answer analytics.
        Answers.getInstance().logCustom(new CustomEvent(appEvent.name()));
    }
}
