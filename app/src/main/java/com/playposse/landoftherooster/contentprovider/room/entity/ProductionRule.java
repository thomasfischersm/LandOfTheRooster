package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.playposse.landoftherooster.util.StringUtil;

import java.util.List;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * The Room entity for a production rule.
 */
@Entity(tableName = "production_rule",
        indices = {
                @Index("building_type_id"),
                @Index("output_resource_type_id"),
                @Index("output_unit_type_id")},
        foreignKeys = {
                @ForeignKey(
                        entity = ResourceType.class,
                        parentColumns = "id",
                        childColumns = "output_resource_type_id",
                        onDelete = NO_ACTION),
                @ForeignKey(
                        entity = UnitType.class,
                        parentColumns = "id",
                        childColumns = "output_unit_type_id",
                        onDelete = NO_ACTION)})
public class ProductionRule {

    @PrimaryKey(autoGenerate = false)
    private int id;

    @ColumnInfo(name = "building_type_id")
    @NonNull
    private long buildingTypeId;

    @ColumnInfo(name = "input_resource_type_ids")
    private String inputResourceTypeIds;

    @ColumnInfo(name = "output_resource_type_ids")
    private String inputUnitTypeIds;

    @ColumnInfo(name = "output_resource_type_id")
    private Long outputResourceTypeId;

    @ColumnInfo(name = "output_unit_type_id")
    private Long outputUnitTypeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public long getBuildingTypeId() {
        return buildingTypeId;
    }

    public void setBuildingTypeId(@NonNull long buildingTypeId) {
        this.buildingTypeId = buildingTypeId;
    }

    public String getInputResourceTypeIds() {
        return inputResourceTypeIds;
    }

    public void setInputResourceTypeIds(String inputResourceTypeIds) {
        this.inputResourceTypeIds = inputResourceTypeIds;
    }

    public String getInputUnitTypeIds() {
        return inputUnitTypeIds;
    }

    public void setInputUnitTypeIds(String inputUnitTypeIds) {
        this.inputUnitTypeIds = inputUnitTypeIds;
    }

    public Long getOutputResourceTypeId() {
        return outputResourceTypeId;
    }

    public void setOutputResourceTypeId(Long outputResourceTypeId) {
        this.outputResourceTypeId = outputResourceTypeId;
    }

    public Long getOutputUnitTypeId() {
        return outputUnitTypeId;
    }

    public void setOutputUnitTypeId(Long outputUnitTypeId) {
        this.outputUnitTypeId = outputUnitTypeId;
    }

    public boolean isFree() {
        return StringUtil.isEmpty(inputResourceTypeIds) && StringUtil.isEmpty(inputUnitTypeIds);
    }

    public List<Long> getSplitInputResourceTypeIds() {
        return StringUtil.splitToLongList(getInputResourceTypeIds());
    }

    public List<Long> getSplitInputUnitTypeIds() {
        return StringUtil.splitToLongList(getInputUnitTypeIds());
    }
}
