package com.playposse.landoftherooster.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * A connector class that manages all the navigation between activities.
 */
public final class ActivityNavigator {

    private static final String BUILDING_ID_EXTRA_PARAMETER = "building_id";
    private static final int NULL_CONSTANT = -1;

    private ActivityNavigator() {}

    public static void startKingdomActivity(Context context) {
        context.startActivity(new Intent(context, KingdomActivity.class));
    }

    public static void startBattleActivity(Context context, long buildingId) {
        Intent intent = new Intent(context, BattleActivity.class);
        intent.putExtra(BUILDING_ID_EXTRA_PARAMETER, buildingId);
        context.startActivity(intent);
    }

    @Nullable
    public static Long getBuildingId(Intent intent) {
        long buildingId = intent.getIntExtra(BUILDING_ID_EXTRA_PARAMETER, NULL_CONSTANT);
        return (buildingId != NULL_CONSTANT) ? buildingId : null;
    }
}
