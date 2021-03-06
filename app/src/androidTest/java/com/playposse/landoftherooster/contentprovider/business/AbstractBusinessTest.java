package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import com.google.android.gms.maps.model.LatLng;
import com.playposse.landoftherooster.RoosterApplication;
import com.playposse.landoftherooster.TestData;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepository;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingRepository;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingZoneRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.services.GameBackgroundService;

import org.junit.After;
import org.junit.Before;

/**
 * A base class that sets up a test environment for the {@link BusinessEngine}.
 */
public abstract class AbstractBusinessTest extends TestData {

    protected RoosterDao dao;
    protected BusinessEngine businessEngine;

    @Before
    public void setUp() throws InterruptedException {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();

        // Stop GameBackgroundService, so that it can't interfere with tests.
        targetContext.stopService(new Intent(targetContext, GameBackgroundService.class));

        // Set a default location.
        BuildingZoneRepository.get(dao)
                .updateLocation(new LatLng(LATITUDE, LONGITUDE));

        // Wait for debug data to be complete.
        while (!RoosterApplication.isDebugDataComplete()) {
            Thread.sleep(10);
        }

        // Clear data that test may generate.
        dao.deleteMapMarkers();
        dao.deleteBuildings();
        dao.deleteResources();
        dao.deleteUnits();

        // Start business engine.
        BuildingRepository.stop();
        businessEngine = BusinessEngine.get();
        businessEngine.start(targetContext);
        BuildingRepository.stop();
    }

    @After
    public void tearDown() {
        BusinessEngine.get().stop();
        BuildingDiscoveryRepository.get(dao).reset();
        BuildingRepository.stop();
    }
}
