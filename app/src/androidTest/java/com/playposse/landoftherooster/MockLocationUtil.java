package com.playposse.landoftherooster;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;

/**
 * A utility to help set a mock location.
 */
public final class MockLocationUtil {

    private static final String LOG_TAG = MockLocationUtil.class.getSimpleName();

    private MockLocationUtil() {
    }

    static void enableMockLocationProvider(LocationManager locationManager) {
        locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        locationManager.setTestProviderStatus(
                LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis());
    }

    public static void disableMockLocationProvider(LocationManager locationManager) {
        locationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
        locationManager.clearTestProviderStatus(LocationManager.GPS_PROVIDER);
        locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
    }

    static void setMockLocation(LocationManager locationManager, Location location) {
        setMockLocation(locationManager, location.getLatitude(), location.getLongitude());
    }

    static void setMockLocation(
            LocationManager locationManager,
            double latitude,
            double longitude) {

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(1);
        location.setBearingAccuracyDegrees(1);
        location.setSpeedAccuracyMetersPerSecond(1);
        location.setVerticalAccuracyMeters(1);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(System.nanoTime());

        for (int i = 0; i < 3; i++) {
            location = new Location(location);
            location.setTime(System.currentTimeMillis());
            location.setElapsedRealtimeNanos(System.nanoTime());

            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
            Log.i(LOG_TAG, "setMockLocation: Set location to: latitude: " + latitude
                    + " longitude: " + longitude);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "setMockLocation: ", ex);
            }
        }
    }

    static Location moveNorth(Location startLocation, int distanceInMeters) {
        Location newLocation = new Location(startLocation);
        double deltaAngle = (180 / Math.PI) * (distanceInMeters / 6378137.0);
        newLocation.setLatitude(newLocation.getLatitude() + deltaAngle);

        Log.i(LOG_TAG, "moveNorth: Attempted distance: " + distanceInMeters);
        Log.i(LOG_TAG, "moveNorth: Actual distance: " + startLocation.distanceTo(newLocation));
        Log.i(LOG_TAG, "moveNorth: new location: latitude: " + newLocation.getLatitude()
                + " longitude: " + newLocation.getLongitude());

        return newLocation;
    }
}
