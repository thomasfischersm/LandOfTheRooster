package com.playposse.landoftherooster;

import android.content.Context;

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
    public static int PEASANT_ID;

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
     *
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



    private GameConfig() {}

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
    }

    private static int get(Context context, int resId) {
        return context.getResources().getInteger(resId);
    }
}
