package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;

/**
 * A data structure that captures the result of a query to see how many units per type are inside
 * of a building.
 */
public class UnitCountByType {

    @ColumnInfo(name = "unit_type_id")
    private long unitTypeId;

    @ColumnInfo(name = "count")
    private int count;

    public long getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(long unitTypeId) {
        this.unitTypeId = unitTypeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
