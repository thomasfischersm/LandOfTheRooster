package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nullable;

/**
 * A Room entity that describes the instance of a unit.
 */
@Entity(foreignKeys = @ForeignKey(
        entity = UnitType.class,
        parentColumns = "id",
        childColumns = "unit_type_id"),
        indices = @Index("unit_type_id"))
public class Unit {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "unit_type_id")
    private int unitTypeId;

    private int health;

    @ColumnInfo(name = "located_at_building_id")
    @Nullable
    private Long locatedAtBuildingId;

    public Unit() {
    }

    @Ignore
    public Unit(int unitTypeId, int health, @Nullable Long locatedAtBuildingId) {
        this.unitTypeId = unitTypeId;
        this.health = health;
        this.locatedAtBuildingId = locatedAtBuildingId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(int unitTypeId) {
        this.unitTypeId = unitTypeId;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    @Nullable
    public Long getLocatedAtBuildingId() {
        return locatedAtBuildingId;
    }

    public void setLocatedAtBuildingId(@Nullable Long locatedAtBuildingId) {
        this.locatedAtBuildingId = locatedAtBuildingId;
    }
}
