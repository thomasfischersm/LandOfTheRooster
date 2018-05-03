package com.playposse.landoftherooster.contentprovider.business;

/**
 * Created by thoma on 5/2/2018.
 */
public class ResourceItem extends Item {


    private final long resourceTypeId;

    public ResourceItem(long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public long getResourceTypeId() {
        return resourceTypeId;
    }
}
