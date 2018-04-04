package com.playposse.landoftherooster;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.activity.KingdomActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * A UI test that walks around the map a bit.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicWalkingUiTest {

    private static final String LOG_TAG = BasicWalkingUiTest.class.getSimpleName();

    @Rule
    public ActivityTestRule<KingdomActivity> activityRule =
            new ActivityTestRule<>(KingdomActivity.class);

    private Context context;
    private Context targetContext;

    @Before
    public void setUp() throws IOException {
        Log.i(LOG_TAG, "setUp: The BasicWalkingUiTest is getting initialized.");

        context = InstrumentationRegistry.getContext();
        targetContext = InstrumentationRegistry.getTargetContext();

        // Reset the database
//        targetContext.deleteDatabase(RoosterDatabaseHelper.DB_NAME);
//        MigrationTestHelper migrationTestHelper =
//                new MigrationTestHelper(
//                        InstrumentationRegistry.getInstrumentation(),
//                        RoosterDatabase.class.getCanonicalName(),
//                        new FrameworkSQLiteOpenHelperFactory());
//        migrationTestHelper.createDatabase(RoosterDatabaseHelper.DB_NAME, 9);

        Log.i(LOG_TAG, "setUp: The BasicWalkingUiTest is done initializing.");
    }

    @Test
    public void walk() throws InterruptedException {
        LocationManager locationManager =
                (LocationManager) targetContext.getSystemService(Context.LOCATION_SERVICE);
        enableMockLocationProvider(locationManager);

        setMockLocation(locationManager, 34.001, -118.485);
        Thread.sleep(1_000);
        setMockLocation(locationManager, 34.002, -118.485);
        Thread.sleep(1_000);
        setMockLocation(locationManager, 34.003, -118.485);
        Thread.sleep(1_000);
        setMockLocation(locationManager, 34.004, -118.485);
        Thread.sleep(1_000);
        setMockLocation(locationManager, 34.005, -118.485);
        Thread.sleep(1_000);
        setMockLocation(locationManager, 34.006, -118.485);
        Thread.sleep(1_000);
    }

    private void setMockLocation(
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

        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
    }

    private void enableMockLocationProvider(LocationManager locationManager) {
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
}
