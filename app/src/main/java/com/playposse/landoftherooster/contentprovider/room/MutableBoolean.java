package com.playposse.landoftherooster.contentprovider.room;

/**
 * A boolean that can be modified. This helps for passing an object into another method and getting
 * a value returned.
 */
public class MutableBoolean {

    private boolean value;

    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
