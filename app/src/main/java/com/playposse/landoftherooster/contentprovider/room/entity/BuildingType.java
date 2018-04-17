package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * The Room entity for the building type.
 */
@Entity(tableName = "building_type",
        foreignKeys = {
                @ForeignKey(
                        entity = UnitType.class,
                        parentColumns = "id",
                        childColumns = "enemy_unit_type_id",
                        onDelete = NO_ACTION),
                @ForeignKey(
                        entity = ResourceType.class,
                        parentColumns = "id",
                        childColumns = "conquest_prize_resource_type_id",
                        onDelete = NO_ACTION)})
public class BuildingType {

    @PrimaryKey(autoGenerate = false)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String icon;

    @ColumnInfo(name = "min_distance_meters")
    private Integer minDistanceMeters;

    @ColumnInfo(name = "max_distance_meters")
    private Integer maxDistanceMeters;

    @ColumnInfo(name = "enemy_unit_count")
    private Integer enemyUnitCount;

    @ColumnInfo(name = "enemy_unit_type_id")
    private Integer enemyUnitTypeId;

    @ColumnInfo(name = "conquest_prize_resource_type_id")
    private Integer conquestPrizeResourceTypeId;

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

    public Integer getEnemyUnitCount() {
        return enemyUnitCount;
    }

    public void setEnemyUnitCount(Integer enemyUnitCount) {
        this.enemyUnitCount = enemyUnitCount;
    }

    public Integer getEnemyUnitTypeId() {
        return enemyUnitTypeId;
    }

    public void setEnemyUnitTypeId(Integer enemyUnitTypeId) {
        this.enemyUnitTypeId = enemyUnitTypeId;
    }

    public Integer getConquestPrizeResourceTypeId() {
        return conquestPrizeResourceTypeId;
    }

    public void setConquestPrizeResourceTypeId(Integer conquestPrizeResourceTypeId) {
        this.conquestPrizeResourceTypeId = conquestPrizeResourceTypeId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("icon", icon)
                .add("minDistanceMeters", minDistanceMeters)
                .add("maxDistanceMeters", maxDistanceMeters)
                .add("enemyUnitCount", enemyUnitCount)
                .add("enemyUnitTypeId", enemyUnitTypeId)
                .add("conquestPrizeResourceTypeId", conquestPrizeResourceTypeId)
                .toString();
    }
}
