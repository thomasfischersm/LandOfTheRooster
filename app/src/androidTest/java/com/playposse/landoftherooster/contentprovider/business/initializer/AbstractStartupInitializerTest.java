package com.playposse.landoftherooster.contentprovider.business.initializer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import org.junit.After;
import org.junit.Before;

/**
 * A base test class that provides methods for testing data and a {@link RoosterDao} accessible as a
 * field.
 */
public class AbstractStartupInitializerTest extends TestData {

    protected RoosterDao dao;
    protected Context targetContext;

    @Before
    public void setUp() throws InterruptedException {
        targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();

        // Ensure that BusinessEngine hasn't been initialized yet.
        BusinessEngine.get()
                .stop();
    }

    @After
    public void tearDown() {
        BusinessEngine.get().stop();
        BuildingDiscoveryRepository.get(dao).reset();
        BuildingRepository.stop();
    }
}
