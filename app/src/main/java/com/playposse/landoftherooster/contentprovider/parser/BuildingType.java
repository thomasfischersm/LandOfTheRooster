package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read building types.
 */
public class BuildingType {

    private int id;
    private String name;
    private String icon;
    private int producedResourceTypeId;
    private int minDistanceMeters;
    private int maxDistanceMeters;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getProducedResourceTypeId() {
        return producedResourceTypeId;
    }

    public int getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public int getMaxDistanceMeters() {
        return maxDistanceMeters;
    }
}
