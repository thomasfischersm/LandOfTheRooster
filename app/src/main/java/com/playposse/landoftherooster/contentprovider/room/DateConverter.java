package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * A Room converter that converts dates to longs and back.
 */
public final class DateConverter {

    private DateConverter() {}

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return (date == null) ? null : date.getTime();
    }
}
