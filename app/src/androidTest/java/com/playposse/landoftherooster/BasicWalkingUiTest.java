package com.playposse.landoftherooster;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.landoftherooster.activity.KingdomActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * A UI test that walks around the map a bit.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicWalkingUiTest {

    private static final String LOG_TAG = BasicWalkingUiTest.class.getSimpleName();

    private static final int DEFAULT_BUILDING_DISTANCE = 310;

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

        LocationManager locationManager =
                (LocationManager) targetContext.getSystemService(Context.LOCATION_SERVICE);
        MockLocationUtil.enableMockLocationProvider(locationManager);

        Log.i(LOG_TAG, "setUp: The BasicWalkingUiTest is done initializing.");
    }

    @After
    public void tearDown() {
        LocationManager locationManager =
                (LocationManager) targetContext.getSystemService(Context.LOCATION_SERVICE);
        MockLocationUtil.disableMockLocationProvider(locationManager);
    }

    @Test
    public void walk() throws InterruptedException {
        LocationManager locationManager =
                (LocationManager) targetContext.getSystemService(Context.LOCATION_SERVICE);

        // Move to castle.
        Location castleLocation = new Location(LocationManager.GPS_PROVIDER);
        castleLocation.setLatitude(34.001);
        castleLocation.setLongitude(-118.485);
        MockLocationUtil.setMockLocation(locationManager, castleLocation);

        // Move to wheat field.
        Location fieldLocation =
                MockLocationUtil.moveNorth(castleLocation, DEFAULT_BUILDING_DISTANCE);
        MockLocationUtil.setMockLocation(locationManager, fieldLocation);
        onView(withId(R.id.resource_recycler_view))
                .check(matches(hasDescendant(withText("wheat: 1"))));

        // Move to mill.
        Location millLocation =
                MockLocationUtil.moveNorth(fieldLocation, DEFAULT_BUILDING_DISTANCE);
        MockLocationUtil.setMockLocation(locationManager, millLocation);
        onView(withId(R.id.resource_recycler_view))
                .check(matches(hasDescendant(withText("flour: 1"))));

        // Move to mill.
        Location bakeryLocation =
                MockLocationUtil.moveNorth(millLocation, DEFAULT_BUILDING_DISTANCE);
        MockLocationUtil.setMockLocation(locationManager, bakeryLocation);
        onView(withId(R.id.resource_recycler_view))
                .check(matches(hasDescendant(withText("bread: 1"))));

        // Move to village.
        Location villageLocation =
                MockLocationUtil.moveNorth(bakeryLocation, DEFAULT_BUILDING_DISTANCE);
        MockLocationUtil.setMockLocation(locationManager, villageLocation);
        onView(withId(R.id.unit_recycler_view))
                .check(matches(hasDescendant(withText("peasant: 1"))));

        MockLocationUtil.setMockLocation(locationManager, 34.003, -118.485);
        Thread.sleep(1_000);
        MockLocationUtil.setMockLocation(locationManager, 34.004, -118.485);
        Thread.sleep(1_000);
        MockLocationUtil.setMockLocation(locationManager, 34.005, -118.485);
        Thread.sleep(1_000);
        MockLocationUtil.setMockLocation(locationManager, 34.006, -118.485);


        Thread.sleep(10_000);
    }
}
