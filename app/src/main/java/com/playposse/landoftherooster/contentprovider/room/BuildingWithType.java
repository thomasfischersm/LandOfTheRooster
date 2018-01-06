package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.Embedded;

/**
 * A Room entity that combines {@link Building} and {@link BuildingType}.
 */
public class BuildingWithType {

    @Embedded
    private Building building;

    @Embedded(prefix = "type_")
    private BuildingType buildingType;

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }
}
