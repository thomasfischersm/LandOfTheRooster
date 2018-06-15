package com.playposse.landoftherooster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.ExecutionException;

/**
 * Neat packaging of accessing the user's current location.
 */
public class ConvenientLocationProvider {

    private static final int FASTEST_INTERVAL_MS = 500;

    private final int intervalMs;
    private final Callback callback;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback = new ConvenientLocationCallback();

    public ConvenientLocationProvider(Context context, int intervalMs, Callback callback)
            throws ExecutionException, InterruptedException {

        this.intervalMs = intervalMs;
        this.callback = callback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Wait until location permissions have been gratned.
        LocationPermissionFutureTask permissionTask = new LocationPermissionFutureTask(context);
        permissionTask.run();
        permissionTask.get();

        requestLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(intervalMs);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public void close() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    /**
     * Receiver of location updates.
     */
    private class ConvenientLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(final LocationResult locationResult) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Location location = locationResult.getLastLocation();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    callback.onNewLocation(latLng);

                    return null;
                }
            }.execute();
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
    }
}
