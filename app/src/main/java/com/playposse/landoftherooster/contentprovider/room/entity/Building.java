package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * A Room entity for the buildings of the player.
 */
@Entity(tableName = "building",
        indices = @Index("building_type_id"),
        foreignKeys = @ForeignKey(
                entity = BuildingType.class,
                parentColumns = "id",
                childColumns = "building_type_id",
                onDelete = NO_ACTION))
public class Building {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "building_type_id")
    private long buildingTypeId;

    private double latitude;
    private double longitude;

    @ColumnInfo(name = "last_conquest")
    @Nullable
    private Date lastConquest;

    @ColumnInfo(name = "production_start")
    @Nullable
    private Date productionStart;

    @ColumnInfo(name = "healing_started")
    @Nullable
    private Date healingStarted;

    public Building() {
    }

    @Ignore
    public Building(long buildingTypeId, double latitude, double longitude) {
        this.buildingTypeId = buildingTypeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public Building(Building other) {
        id = other.id;
        buildingTypeId = other.buildingTypeId;
        latitude = other.latitude;
        longitude = other.longitude;
        lastConquest = copy(other.lastConquest);
        this.productionStart = copy(other.productionStart);
        healingStarted = copy(other.healingStarted);
    }

    @Nullable
    private static Date copy(@Nullable Date other) {
        if (other != null) {
            return new Date(other.getTime());
        } else {
            return null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBuildingTypeId() {
        return buildingTypeId;
    }

    public void setBuildingTypeId(long buildingTypeId) {
        this.buildingTypeId = buildingTypeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Nullable
    public Date getLastConquest() {
        return lastConquest;
    }

    public void setLastConquest(@Nullable Date lastConquest) {
        this.lastConquest = lastConquest;
    }

    @Nullable
    public Date getProductionStart() {
        return productionStart;
    }

    public void setProductionStart(@Nullable Date productionStart) {
        this.productionStart = productionStart;
    }

    @Nullable
    public Date getHealingStarted() {
        return healingStarted;
    }

    public void setHealingStarted(@Nullable Date healingStarted) {
        this.healingStarted = healingStarted;
    }
}
