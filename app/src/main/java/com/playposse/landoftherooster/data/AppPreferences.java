package com.playposse.landoftherooster.data;

import android.content.Context;

import com.playposse.landoftherooster.util.BasePreferences;

/**
 * Central registry for all shared preferences.
 */
public class AppPreferences {

    private static final String SHARED_PREFERENCES_NAME = "ourPreferences";

    private static final String HAS_SEEN_INTRO_DECK = "hasSeenIntroDeck";

    private static final boolean HAS_SEEN_INTRO_DECK_DEFAULT = false;

    private static final BasePreferences basePreferences =
            new BasePreferences(SHARED_PREFERENCES_NAME);

    public static boolean hasSeenIntroDeck(Context context) {
        return basePreferences.getBoolean(
                context,
                HAS_SEEN_INTRO_DECK,
                HAS_SEEN_INTRO_DECK_DEFAULT);
    }

    public static void setHasSeenIntroDeck(Context context, boolean value) {
        basePreferences.setBoolean(context, HAS_SEEN_INTRO_DECK, value);
    }
}
