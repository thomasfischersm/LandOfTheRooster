package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.Embedded;

/**
 * A Room entity that combines {@link Unit} and {@link UnitType}.
 */
public class UnitWithType {

    @Embedded
    private Unit unit;

    @Embedded(prefix = "type_")
    private UnitType type;

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public UnitType getType() {
        return type;
    }

    public void setType(UnitType type) {
        this.type = type;
    }
}
