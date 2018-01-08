package com.playposse.landoftherooster.services;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * An interface for non-Android services to implement that handle game logic relevant to the user's
 * location.
 */
public interface ILocationAwareService {

    void onLocationUpdate(LatLng latLng);
}
