package com.playposse.landoftherooster.analytics;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A util to log analytics to Firebase, Fabric, and Google Analytics.
 */
public final class Analytics {

    private enum AnalyticsEvent {
        BUSINESS_EVENT
    }

    private static final String EVENT_NAME_ATTRIBUTE = "eventName";
    private static final String DURATION_MS_ATTRIBUTE = "durationMs";

    private Analytics() {
    }

    public static void logBusinessEvent(BusinessEvent event, long start) {
        long end = System.currentTimeMillis();

        Answers.getInstance().logCustom(new CustomEvent(AnalyticsEvent.BUSINESS_EVENT.name())
                .putCustomAttribute(EVENT_NAME_ATTRIBUTE, event.getClass().getSimpleName())
                .putCustomAttribute(DURATION_MS_ATTRIBUTE, end - start));
    }
}
