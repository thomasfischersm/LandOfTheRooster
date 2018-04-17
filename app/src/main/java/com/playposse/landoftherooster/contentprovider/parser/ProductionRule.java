package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read production rules.
 */
public class ProductionRule {

    private int id;
    private int buildingId;
    private String inputResourceTypeIds;
    private String inputUnitTypeIds;
    private Integer outputResourceTypeId;
    private Integer outputUnitTypeId;

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

    public Integer getOutputResourceTypeId() {
        return outputResourceTypeId;
    }

    public Integer getOutputUnitTypeId() {
        return outputUnitTypeId;
    }
}
