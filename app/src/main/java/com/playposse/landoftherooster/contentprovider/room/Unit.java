package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

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
    private int id;

    @ColumnInfo(name = "unit_type_id")
    private int unitTypeId;

    private int health;

    @ColumnInfo(name = "located_at_building_id")
    private Integer locatedAtBuildingId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Integer getLocatedAtBuildingId() {
        return locatedAtBuildingId;
    }

    public void setLocatedAtBuildingId(Integer locatedAtBuildingId) {
        this.locatedAtBuildingId = locatedAtBuildingId;
    }
}
