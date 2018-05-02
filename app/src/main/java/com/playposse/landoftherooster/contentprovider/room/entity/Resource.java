package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import javax.annotation.Nullable;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * A Room entity that stores how many resources the user has of a particular type.
 */
@Entity(foreignKeys = @ForeignKey(entity = ResourceType.class,
        parentColumns = "id",
        childColumns = "resource_type_id",
        onDelete = NO_ACTION),
        indices = @Index("resource_type_id"))
public class Resource {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "resource_type_id")
    @NonNull
    private long resourceTypeId;

    @NonNull
    private int amount;

    @ColumnInfo(name = "located_at_building_id")
    private Long locatedAtBuildingId;

    public Resource() {
    }

    @Ignore
    public Resource(
            @NonNull long resourceTypeId,
            @NonNull int amount,
            @Nullable Long locatedAtBuildingId) {

        this.resourceTypeId = resourceTypeId;
        this.amount = amount;
        this.locatedAtBuildingId = locatedAtBuildingId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public long getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(@NonNull long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    @NonNull
    public int getAmount() {
        return amount;
    }

    public void setAmount(@NonNull int amount) {
        this.amount = amount;
    }

    public Long getLocatedAtBuildingId() {
        return locatedAtBuildingId;
    }

    public void setLocatedAtBuildingId(Long locatedAtBuildingId) {
        this.locatedAtBuildingId = locatedAtBuildingId;
    }
}
