package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

import javax.annotation.Nullable;

/**
 * A {@link BusinessEvent} that gets triggered when a user taps on a building icon on the map.
 */
public class UserTapsBuildingMarkerEvent extends BusinessEvent {

    public UserTapsBuildingMarkerEvent(@Nullable Long buildingId) {
        super(buildingId);
    }
}
