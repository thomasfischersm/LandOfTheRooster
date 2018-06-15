package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * A ROOM data type for resource types.
 */
@Entity(tableName = "resource_type")
public class ResourceType {

    @PrimaryKey(autoGenerate = false)
    private long id;

    @NonNull
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
