package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read building types.
 */
public class BuildingType {

    private int id;
    private String name;
    private String icon;
    private Integer producedResourceTypeId;
    private Integer minDistanceMeters;
    private Integer maxDistanceMeters;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getProducedResourceTypeId() {
        return producedResourceTypeId;
    }

    public Integer getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public Integer getMaxDistanceMeters() {
        return maxDistanceMeters;
    }
}
