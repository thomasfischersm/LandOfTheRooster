package com.playposse.landoftherooster.util;

import java.util.Random;

/**
 * A random number generator, which produces a random number between a min and max number.
 */
public class MinMaxRandom extends Random {

    public int nextInt(int min, int max) {
        if (min == max) {
            return min;
        }

        return nextInt(max - min) + min;
    }
}
