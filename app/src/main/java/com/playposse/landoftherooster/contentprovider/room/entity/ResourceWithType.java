package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

/**
 * A Room entity that combines {@link Resource} and {@link ResourceType}.
 */
public class ResourceWithType {

    @Embedded
    private Resource resource;

    @Embedded(prefix = "type_")
    private ResourceType type;

    public ResourceWithType() {
    }

    @Ignore
    public ResourceWithType(Resource resource, ResourceType type) {
        this.resource = resource;
        this.type = type;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }
}