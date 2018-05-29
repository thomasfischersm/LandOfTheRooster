package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

import com.playposse.landoftherooster.GameConfig;

/**
 * A Room entity that combines {@link Unit} and {@link UnitType}.
 */
public class UnitWithType {

    @Embedded
    private Unit unit;

    @Embedded(prefix = "type_")
    private UnitType type;

    public UnitWithType() {
    }

    @Ignore
    public UnitWithType(Unit unit, UnitType type) {
        this.unit = unit;
        this.type = type;
    }

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

    public boolean isInjured() {
        return unit.getHealth() < type.getHealth();
    }

    public int getInjury() {
        return type.getHealth() - unit.getHealth();
    }

    public long getHealingTimeMs(int peasantCount) {
        return ((long) getInjury())
                * GameConfig.HEALING_PER_HEALTH_POINT_DURATION_MS
                / peasantCount;
    }
}
