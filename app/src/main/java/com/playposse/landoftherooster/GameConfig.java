package com.playposse.landoftherooster;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;

/**
 * A global constant class that reads its values from game_config.xml during the application start.
 */
public final class GameConfig {

    /**
     * Base amount of peasants in each building. A building is implied to have at least one peasant,
     * who cannot leave. To save database space, this peasant doesn't have a unit instance.
     */
    public static int IMPLIED_PEASANT_COUNT;

    /**
     * The unitTypeId for a peasant. This should be more configurable in the future. It implies
     * which units can work inside of a building.
     */
    public static long PEASANT_ID;

    /**
     * The maximum amount of peasants that can work in a building.
     */
    public static int MAX_PEASANT_BUILDING_CAPACITY;

    /**
     * How many units of a conquest prize the user receives for winning a battle.
     */
    public static int DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT;

    /**
     * The first building type id that the user discovers.
     * <p>
     * <p>Note: This is actually 1 smaller than the actual id.
     */
    public static int INITIAL_BUILDING_TYPE_ID;

    /**
     * An additional distance that the user can go while still being able to discover a building.
     * <p>
     * <p>Each building type has a min and a max. The discovery service decides on an actual
     * distance somewhere between those maximum. As long as the user has walked the distance, a
     * discovery happens. However, to prevent a really far building from all other buildings, a
     * safeguard prevents the discovery when the user exceeds the max distance.
     * <p>
     * <p>While that is good, if the actual distance and the max distance is very close, GPS
     * inaccuracy could make it hard to for the user to trigger. So a fudge factor is added to max
     * to ensure that a reasonable discovery is made.
     */
    public static int MAX_GRACE_DISTANCE;

    /**
     * The radius in meters within a building can be interacted with. This counteracts the
     * inaccuracy of the GPS location.
     */
    public static int INTERACTION_RADIUS;

    /**
     * Time in ms that it takes for a battle building to respawn units.
     */
    public static int BATTLE_RESPAWN_DURATION;

    /**
     * Minutes that it takes a building to produce a new item with 1 peasant.
     */
    public static int PRODUCTION_CYCLE_MINUTES;

    public static int PRODUCTION_CYCLE_MS;

    /**
     * Background drawable for the oval background of a map marker to represent the building ready
     * status.
     */
    public static Drawable BUILDING_READY_BG;

    /**
     * Color for the circle on the building map marker to indicate a pending production run.
     */
    public static int PENDING_PRODUCTION_COLOR;

    /**
     * Color for the circle on the building map marker to indicate a completed production run.
     */
    public static int COMPLETED_PRODUCTION_COLOR;

    /**
     * The maximum number of production circles that are drawn on the building map marker.
     */
    public static int MAX_PRODUCTION_CIRCLE_COUNT;

    /**
     * The margin to the edge of the building icons for production circles on the building map
     * marker.
     */
    public static float PRODUCTION_CIRCLE_MARGIN;

    /**
     * The radius for production circles on the building map marker.
     */
    public static float PRODUCTION_CIRCLE_RADIUS;

    /**
     * Minutes that it takes to heal one health point for unit.
     */
    public static int HEALING_PER_HEALTH_POINT_DURATION_MINUTES;

    private GameConfig() {
    }

    static void init(Context context) {
        IMPLIED_PEASANT_COUNT = get(context, R.integer.implied_peasant_count);
        PEASANT_ID = get(context, R.integer.implied_peasant_count);
        MAX_PEASANT_BUILDING_CAPACITY = get(context, R.integer.max_peasant_building_capacity);
        DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT =
                get(context, R.integer.default_conquest_prize_resource_amount);
        INITIAL_BUILDING_TYPE_ID = get(context, R.integer.initial_building_type_id);
        MAX_GRACE_DISTANCE = get(context, R.integer.max_grace_distance);
        INTERACTION_RADIUS = get(context, R.integer.interaction_radius);
        BATTLE_RESPAWN_DURATION = get(context, R.integer.battle_respawn_duration);
        PRODUCTION_CYCLE_MINUTES = get(context, R.integer.production_cycle_minutes);
        PRODUCTION_CYCLE_MS = PRODUCTION_CYCLE_MINUTES * 60 * 1_000;
        BUILDING_READY_BG = getDrawable(context, R.drawable.building_ready_bg);
        PENDING_PRODUCTION_COLOR = getColor(context, R.color.pending_production_color);
        COMPLETED_PRODUCTION_COLOR = getColor(context, R.color.completed_production_color);
        MAX_PRODUCTION_CIRCLE_COUNT = get(context, R.integer.max_production_circle_count);
        PRODUCTION_CIRCLE_MARGIN = getDimension(context, R.dimen.production_circle_margin);
        PRODUCTION_CIRCLE_RADIUS = getDimension(context, R.dimen.production_circle_radius);
        HEALING_PER_HEALTH_POINT_DURATION_MINUTES =
                get(context, R.integer.healing_per_health_point_duration_minutes);
    }

    private static int get(Context context, @IntegerRes int resId) {
        return context.getResources().getInteger(resId);
    }

    private static int getColor(Context context, @ColorRes int resId) {
        return context.getResources().getColor(resId);
    }

    private static Drawable getDrawable(Context context, @DrawableRes int resId) {
        return context.getResources().getDrawable(resId);
    }

    private static float getDimension(Context context, @DimenRes int resId) {
        return context.getResources().getDimension(resId);
    }
}
