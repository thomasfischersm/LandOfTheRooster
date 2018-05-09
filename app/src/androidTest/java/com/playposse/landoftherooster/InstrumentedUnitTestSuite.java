package com.playposse.landoftherooster;

import com.playposse.landoftherooster.contentprovider.room.RoosterDaoTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngineTest;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionEndedEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.LocationUpdateEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEventTest;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suite for all instrumented tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BuildingDiscoveryRepositoryTest.class,
        BusinessEngineTest.class,
        DaoEventRegistryTest.class,
        ItemProductionEndedEventTest.class,
        LocationUpdateEventTest.class,
        RoosterDaoTest.class,
        UserDropsOffItemEventTest.class
})
public class InstrumentedUnitTestSuite {
}
