package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

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
    private int id;

    @ColumnInfo(name = "building_type_id")
    @NonNull
    private int buildingTypeId;

    private float latitude;
    private float longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public int getBuildingTypeId() {
        return buildingTypeId;
    }

    public void setBuildingTypeId(@NonNull int buildingTypeId) {
        this.buildingTypeId = buildingTypeId;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
