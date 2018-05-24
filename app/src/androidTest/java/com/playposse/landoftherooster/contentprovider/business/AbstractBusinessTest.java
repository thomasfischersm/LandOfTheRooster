package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import org.junit.After;
import org.junit.Before;

/**
 * A base class that sets up a test environment for the {@link BusinessEngine}.
 */
public abstract class AbstractBusinessTest extends TestData {

    protected RoosterDao dao;
    protected BusinessEngine businessEngine;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Start business engine.
        businessEngine = BusinessEngine.get();
        businessEngine.start(targetContext);

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();
    }

    @After
    public void tearDown() {
        BusinessEngine.get().stop();
        BuildingDiscoveryRepository.get(dao).reset();
    }
}
