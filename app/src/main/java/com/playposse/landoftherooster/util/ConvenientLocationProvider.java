package com.playposse.landoftherooster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Neat packaging of accessing the user's current location.
 */
public class ConvenientLocationProvider {

    private static final int FASTEST_INTERVAL_MS = 500;

    private final int intervalMs;
    private final Callback callback;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback = new ConvenientLocationCallback();

    public ConvenientLocationProvider(Context context, int intervalMs, Callback callback) {
        this.intervalMs = intervalMs;
        this.callback = callback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (hasLocationPermission(context)) {
            requestLocationUpdates();
        } else {
            callback.onMissingPermission();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(intervalMs);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    public void close() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean hasLocationPermission(Context context) {
        return ContextCompat
                .checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
    }

    /**
     * Receiver of location updates.
     */
    private class ConvenientLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            callback.onNewLocation(latLng);
        }
    }

    /**
     * Callback interface to receive updates about the location and permission problems.
     */
    public interface Callback {

        /**
         * Called when a new location has been identified.
         */
        void onNewLocation(LatLng latLng);

        /**
         * Called when the user hasn't given the app permission to access the location.
         */
        void onMissingPermission();
    }
}
