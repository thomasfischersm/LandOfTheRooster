package com.playposse.landoftherooster.contentprovider.business.event.locationTriggered;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

/**
 * A {@link BusinessEvent} that indicates that the user has walked far enough to discover a new
 * building.
 */
public class LocationUpdateEvent extends BusinessEvent {

    private final LatLng latLng;

    public LocationUpdateEvent(LatLng latLng) {
        super(null);

        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
