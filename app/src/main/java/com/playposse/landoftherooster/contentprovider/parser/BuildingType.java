package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read building types.
 */
public class BuildingType {

    private int id;
    private String name;
    private String icon;
    private Integer producedResourceTypeId;
    private Integer producedUnitTypeId;
    private Integer minDistanceMeters;
    private Integer maxDistanceMeters;
    private Integer enemyUnitCount;
    private Integer enemyUnitTypeId;
    private Integer conquestPrizeResourceTypeId;

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

    public Integer getProducedUnitTypeId() {
        return producedUnitTypeId;
    }

    public Integer getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public Integer getMaxDistanceMeters() {
        return maxDistanceMeters;
    }

    public Integer getEnemyUnitCount() {
        return enemyUnitCount;
    }

    public Integer getEnemyUnitTypeId() {
        return enemyUnitTypeId;
    }

    public Integer getConquestPrizeResourceTypeId() {
        return conquestPrizeResourceTypeId;
    }
}
