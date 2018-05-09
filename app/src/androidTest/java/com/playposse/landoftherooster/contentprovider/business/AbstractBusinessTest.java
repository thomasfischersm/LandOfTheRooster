package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import org.junit.After;
import org.junit.Before;

/**
 * A base class that sets up a test environment for the {@link BusinessEngine}.
 */
public abstract class AbstractBusinessTest {

    protected static final long CASTLE_BUILDING_TYPE_ID = 1;
    protected static final long WHEAT_FIELD_BUILDING_TYPE_ID = 2; // Mill: wheat (1) -> flour (2)
    protected static final long MILL_BUILDING_TYPE_ID = 3; // Mill: wheat (1) -> flour (2)
    protected static final long WHEAT_RESOURCE_TYPE_ID = 1;
    protected static final long FLOUR_RESOURCE_TYPE_ID = 2;
    protected static final long LATITUDE = 34;
    protected static final long LONGITUDE = 118;

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
        BuildingDiscoveryRepository.get(dao).reset();
    }
}
