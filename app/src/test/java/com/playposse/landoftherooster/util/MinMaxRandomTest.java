package com.playposse.landoftherooster.util;

import org.hamcrest.Matchers;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * A test for {@link MinMaxRandom}.
 */
public class MinMaxRandomTest {

    private static final int MIN = 15;
    private static final int MAX = 37;

    @Test
    public void nextInt() {
        MinMaxRandom minMaxRandom = new MinMaxRandom();
        for (int i = 0; i < 100; i++) {
            int num = minMaxRandom.nextInt(MIN, MAX);
            assertThat(num, Matchers.greaterThanOrEqualTo(MIN));
            assertThat(num, Matchers.lessThan(MAX));
        }
    }

    @Test
    public void nextInt_zeroMin() {
        int lowMax = 2;
        MinMaxRandom minMaxRandom = new MinMaxRandom();
        boolean hasZero = false;
        boolean hasOne = false;
        for (int i = 0; i < 200; i++) {
            int num = minMaxRandom.nextInt(0, lowMax);
            assertThat(num, Matchers.greaterThanOrEqualTo(0));
            assertThat(num, Matchers.lessThan(lowMax));

            hasZero = hasZero || (num == 0);
            hasOne = hasOne || (num == 1);
        }

        assertTrue(hasZero);
        assertTrue(hasOne);
    }

    @Test public void nextInt_minEqualsMax() {
        int min = 5;
        int max = 5;

        MinMaxRandom minMaxRandom = new MinMaxRandom();
        for (int i = 0; i < 10; i++) {
            int num = minMaxRandom.nextInt(min, max);
            assertEquals(5, num);
        }
    }
}
