package com.playposse.landoftherooster.util;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A handwritten version of the Java StringJoiner class. The java version requires to set a
 * higher minimum Android version.
 */
public class SimpleStringJoiner {

    private final String separator;
    @Nullable private final String itemPrefix;
    @Nullable private final String itemSuffix;
    private final StringBuilder value = new StringBuilder();

    public SimpleStringJoiner(String separator) {
        this(separator, null, null);
    }

    public SimpleStringJoiner(
            String separator,
            @Nullable String itemPrefix,
            @Nullable String itemSuffix) {

        Objects.requireNonNull(separator);

        this.separator = separator;
        this.itemPrefix = itemPrefix;
        this.itemSuffix = itemSuffix;
    }

    public void add(CharSequence newElement) {
        if (value.length() > 0) {
            value.append(separator);
        }

        if (itemPrefix != null) {
            value.append(itemPrefix);
        }

        value.append(newElement);

        if (itemSuffix != null) {
            value.append(itemSuffix);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
