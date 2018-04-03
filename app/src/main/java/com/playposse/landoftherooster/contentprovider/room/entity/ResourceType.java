package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * A ROOM data type for resource types.
 */
@Entity(tableName = "resource_type",
        indices = {
                @Index("precursor_resource_type_id"),
                @Index("precursor_unit_type_id")},
        foreignKeys = {
                @ForeignKey(entity = ResourceType.class,
                        parentColumns = "id",
                        childColumns = "precursor_resource_type_id",
                        onDelete = NO_ACTION),
                @ForeignKey(entity = UnitType.class,
                        parentColumns = "id",
                        childColumns = "precursor_unit_type_id",
                        onDelete = NO_ACTION)})
public class ResourceType {

    @PrimaryKey(autoGenerate = false)
    private int id;

    @NonNull
    private String name;

    @ColumnInfo(name = "precursor_resource_type_id")
    private Integer precursorResourceTypeId;

    @ColumnInfo(name = "precursor_unit_type_id")
    private Integer precursorUnitTypeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrecursorResourceTypeId() {
        return precursorResourceTypeId;
    }

    public void setPrecursorResourceTypeId(Integer precursorResourceTypeId) {
        this.precursorResourceTypeId = precursorResourceTypeId;
    }

    public Integer getPrecursorUnitTypeId() {
        return precursorUnitTypeId;
    }

    public void setPrecursorUnitTypeId(Integer precursorUnitTypeId) {
        this.precursorUnitTypeId = precursorUnitTypeId;
    }
}
