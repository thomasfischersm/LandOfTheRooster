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
        Intent intent = new Intent(context, KingdomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startStopActivity(Context context) {
        context.startActivity(new Intent(context, StopActivity.class));
    }

    public static void startAboutActivity(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    public static void startPermissionRecoveryActivity(Context context) {
        Intent intent = new Intent(context, PermissionRecoveryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
