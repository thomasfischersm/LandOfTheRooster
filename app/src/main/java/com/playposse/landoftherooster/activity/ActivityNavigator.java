package com.playposse.landoftherooster.activity;

import android.content.Context;
import android.content.Intent;

/**
 * A connector class that manages all the navigation between activities.
 */
public final class ActivityNavigator {

    private ActivityNavigator() {}

    public static void startIntroductionActivity(Context context) {
        context.startActivity(new Intent(context, IntroductionActivity.class));
    }

    public static void startKingdomActivity(Context context) {
        context.startActivity(new Intent(context, KingdomActivity.class));
    }

    public static void startStopActivity(Context context) {
        context.startActivity(new Intent(context, StopActivity.class));
    }
}
