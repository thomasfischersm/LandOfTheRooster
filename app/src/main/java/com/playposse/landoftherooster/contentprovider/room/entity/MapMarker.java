package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import javax.annotation.Nullable;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * A Room entity that represents a marker on the map. To make the processing of the map faster,
 * the map marker information is cached in the database. Changes to these tables will update the
 * map through {@link LiveData}.
 */
@Entity(tableName = "map_marker",
        indices = {
                @Index("building_id"),
                @Index("building_type_id")},
        foreignKeys = {
                @ForeignKey(
                        entity = Building.class,
                        parentColumns = "id",
                        childColumns = "building_id",
                        onDelete = NO_ACTION),
                @ForeignKey(
                        entity = BuildingType.class,
                        parentColumns = "id",
                        childColumns = "building_type_id",
                        onDelete = NO_ACTION)})
public class MapMarker {

    public static final int BUILDING_MARKER_TYPE = 1;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "marker_type")
    private int markerType;

    private String icon;

    private String name;

    @Nullable
    @ColumnInfo(name = "pending_production_count")
    private Integer pendingProductionCount;

    @Nullable
    @ColumnInfo(name = "completed_production_count")
    private Integer completedProductionCount;

    @ColumnInfo(name = "is_ready")
    private boolean isReady;

    @Nullable
    @ColumnInfo(name = "building_id")
    private Long buildingId;

    @Nullable
    @ColumnInfo(name = "building_type_id")
    private Long buildingTypeId;

    @NonNull
    @ColumnInfo(name = "last_modified")
    private Date lastModified;

    public MapMarker() {
    }

    @Ignore
    public MapMarker(
            int markerType,
            @NonNull String icon,
            @NonNull String name,
            @NonNull Integer pendingProductionCount,
            @NonNull Integer completedProductionCount,
            boolean isReady,
            @NonNull Long buildingId,
            @NonNull Long buildingTypeId) {

        this.markerType = markerType;
        this.icon = icon;
        this.name = name;
        this.pendingProductionCount = pendingProductionCount;
        this.completedProductionCount = completedProductionCount;
        this.isReady = isReady;
        this.buildingId = buildingId;
        this.buildingTypeId = buildingTypeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMarkerType() {
        return markerType;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public Integer getPendingProductionCount() {
        return pendingProductionCount;
    }

    public void setPendingProductionCount(@Nullable Integer pendingProductionCount) {
        this.pendingProductionCount = pendingProductionCount;
    }

    @Nullable
    public Integer getCompletedProductionCount() {
        return completedProductionCount;
    }

    public void setCompletedProductionCount(@Nullable Integer completedProductionCount) {
        this.completedProductionCount = completedProductionCount;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    @Nullable
    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(@Nullable Long buildingId) {
        this.buildingId = buildingId;
    }

    @Nullable
    public Long getBuildingTypeId() {
        return buildingTypeId;
    }

    public void setBuildingTypeId(@Nullable Long buildingTypeId) {
        this.buildingTypeId = buildingTypeId;
    }

    @NonNull
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(@NonNull Date lastModified) {
        this.lastModified = lastModified;
    }
}
