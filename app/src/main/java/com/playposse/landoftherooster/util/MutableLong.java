package com.playposse.landoftherooster.util;

/**
 * A long that can be modified. This helps for passing an object into another method and getting
 * a value returned.
 */
public class MutableLong {

    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public MutableLong add(long addition) {
        value += addition;
        return this;
    }

    public MutableLong divide(long divisor) {
        value /= divisor;
        return this;
    }
}
