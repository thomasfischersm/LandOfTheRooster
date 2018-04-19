package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
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
    @NonNull
    private int buildingTypeId;

    private double latitude;
    private double longitude;

    @ColumnInfo(name = "last_conquest")
    @Nullable
    private Date lastConquest;

    @ColumnInfo(name = "last_production")
    @Nullable
    private Date lastProduction;

    public Building() {
    }

    @Ignore
    public Building(@NonNull int buildingTypeId, double latitude, double longitude) {
        this.buildingTypeId = buildingTypeId;
        this.latitude = latitude;
        this.longitude = longitude;

        lastProduction = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public int getBuildingTypeId() {
        return buildingTypeId;
    }

    public void setBuildingTypeId(@NonNull int buildingTypeId) {
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
    public Date getLastProduction() {
        return lastProduction;
    }

    public void setLastProduction(@Nullable Date lastProduction) {
        this.lastProduction = lastProduction;
    }
}
