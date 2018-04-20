package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read production rules.
 */
public class ProductionRule {

    private int id;
    private int buildingId;
    private String inputResourceTypeIds;
    private String inputUnitTypeIds;
    private Long outputResourceTypeId;
    private Long outputUnitTypeId;

    public int getId() {
        return id;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public String getInputResourceTypeIds() {
        return inputResourceTypeIds;
    }

    public String getInputUnitTypeIds() {
        return inputUnitTypeIds;
    }

    public Long getOutputResourceTypeId() {
        return outputResourceTypeId;
    }

    public Long getOutputUnitTypeId() {
        return outputUnitTypeId;
    }
}
