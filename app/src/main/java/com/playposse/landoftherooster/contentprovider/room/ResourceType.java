package com.playposse.landoftherooster.contentprovider.room;

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
        indices = @Index("precursor_id"),
        foreignKeys = @ForeignKey(entity = ResourceType.class,
                parentColumns = "id",
                childColumns = "precursor_id",
                onDelete = NO_ACTION))
public class ResourceType {

    @PrimaryKey(autoGenerate = false)
    private int id;

    @NonNull
    private String name;

    @ColumnInfo(name = "precursor_id")
    private Integer precursorId;

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

    public Integer getPrecursorId() {
        return precursorId;
    }

    public void setPrecursorId(Integer precursorId) {
        this.precursorId = precursorId;
    }
}
