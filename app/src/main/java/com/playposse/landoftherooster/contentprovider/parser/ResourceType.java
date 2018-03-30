package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read resource types.
 */
public class ResourceType {

    private int id;
    private String name;
    private Integer precursorResourceTypeId;
    private Integer precursorUnitTypeId;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrecursorResourceTypeId() {
        return precursorResourceTypeId;
    }

    public Integer getPrecursorUnitTypeId() {
        return precursorUnitTypeId;
    }
}
