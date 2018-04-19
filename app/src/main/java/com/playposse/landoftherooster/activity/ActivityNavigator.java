package com.playposse.landoftherooster.activity;

import android.content.Context;
import android.content.Intent;

/**
 * A connector class that manages all the navigation between activities.
 */
public final class ActivityNavigator {

    private ActivityNavigator() {}

    public static void startKingdomActivity(Context context) {
        context.startActivity(new Intent(context, KingdomActivity.class));
    }
}
