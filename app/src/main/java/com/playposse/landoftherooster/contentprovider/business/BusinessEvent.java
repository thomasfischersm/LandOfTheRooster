package com.playposse.landoftherooster.contentprovider.business;

import java.util.Objects;

/**
 * Base class for events that can trigger business actions.
 */
public class BusinessEvent {

    private final Long buildingId;

    public BusinessEvent(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), buildingId);
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || (getClass() != other.getClass())) {
            return false;
        }

        Long otherBuildingId = ((BusinessEvent) other).getBuildingId();
        return Objects.equals(this.buildingId, otherBuildingId);
    }
}
