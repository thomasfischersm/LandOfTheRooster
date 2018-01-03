package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read resource types.
 */
public class ResourceType {

    private int id;
    private String name;
    private Integer precursorId;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrecursorId() {
        return precursorId;
    }
}
