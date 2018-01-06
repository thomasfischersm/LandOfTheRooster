package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * The Room entity for the building type.
 */
@Entity(tableName = "building_type",
        indices = @Index("produced_resource_type_id"),
        foreignKeys = @ForeignKey(
                entity = BuildingType.class,
                parentColumns = "id",
                childColumns = "produced_resource_type_id",
                onDelete = NO_ACTION))
public class BuildingType {

    @PrimaryKey(autoGenerate = false)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String icon;

    @ColumnInfo(name = "produced_resource_type_id")
    private Integer producedResourceTypeId;

    @ColumnInfo(name = "min_distance_meters")
    private Integer minDistanceMeters;

    @ColumnInfo(name = "max_distance_meters")
    private Integer maxDistanceMeters;

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getProducedResourceTypeId() {
        return producedResourceTypeId;
    }

    public void setProducedResourceTypeId(Integer producedResourceTypeId) {
        this.producedResourceTypeId = producedResourceTypeId;
    }

    public Integer getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public void setMinDistanceMeters(Integer minDistanceMeters) {
        this.minDistanceMeters = minDistanceMeters;
    }

    public Integer getMaxDistanceMeters() {
        return maxDistanceMeters;
    }

    public void setMaxDistanceMeters(Integer maxDistanceMeters) {
        this.maxDistanceMeters = maxDistanceMeters;
    }
}
