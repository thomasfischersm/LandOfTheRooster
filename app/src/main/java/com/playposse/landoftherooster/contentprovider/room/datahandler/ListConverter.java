package com.playposse.landoftherooster.contentprovider.room.datahandler;

import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that converts lists of data objects into lists of ids.
 */
public final class ListConverter {

    private ListConverter() {}

    public static List<Long> toUnitId(List<UnitWithType> unitsWithType) {
        List<Long> result = new ArrayList<>(unitsWithType.size());
        for (UnitWithType unitWithType : unitsWithType) {
            result.add(unitWithType.getUnit().getId());
        }
        return result;
    }
}
