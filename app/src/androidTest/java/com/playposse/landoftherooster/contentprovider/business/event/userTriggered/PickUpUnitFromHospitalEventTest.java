package com.playposse.landoftherooster.contentprovider.business.event.userTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepository;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link PickUpUnitFromHospitalEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpUnitFromHospitalEventTest extends AbstractBusinessTest {

    private static final String LOG_TAG = PickUpUnitFromHospitalEventTest.class.getSimpleName();

    @Test
    public void triggerEvent_singleUnit() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, SOLDIER_UNIT_TYPE_ID, hospitalId);
        Unit unit = units.get(0);
        long unitId = unit.getId();

        // Test action.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(hospitalId, unitId);
        BusinessEngine.get()
                .triggerEvent(event);

        // Assert that unit has joined the user.
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
        UnitWithType resultUnitWithType = unitTypeRepository.queryUnitWithTypeById(unitId);
        assertNull(resultUnitWithType.getUnit().getLocatedAtBuildingId());

        // Assert hospital map marker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());

        // Assert that potential production buildings have turned green.
        // TODO
    }

    @Test
    public void triggerEvent_twoUnits() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        createUnits(dao, 2, SOLDIER_UNIT_TYPE_ID, hospitalId);

        // Get unit references.
        List<Unit> units = dao.getUnits(SOLDIER_UNIT_TYPE_ID, hospitalId);
        long unitId0 = units.get(0).getId();
        long unitId1 = units.get(1).getId();

        // Execute action for first unit.
        PickUpUnitFromHospitalEvent event0 = new PickUpUnitFromHospitalEvent(hospitalId, unitId0);
        BusinessEngine.get()
                .triggerEvent(event0);

        // Assert that unit has joined the user.
        assertEquals(1, dao.getUnitCountJoiningUser(SOLDIER_UNIT_TYPE_ID));
        assertEquals(1, dao.getUnitCount(SOLDIER_UNIT_TYPE_ID, hospitalId));

        // Assert hospital map marker.
        MapMarker mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertTrue(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 1, mapMarker.getCompletedProductionCount());

        // Assert that potential production buildings have turned green.
        // TODO


        // Execute action for second unit.
        PickUpUnitFromHospitalEvent event1 = new PickUpUnitFromHospitalEvent(hospitalId, unitId1);
        BusinessEngine.get()
                .triggerEvent(event1);

        // Assert that unit has joined the user.
        assertEquals(2, dao.getUnitCountJoiningUser(SOLDIER_UNIT_TYPE_ID));
        assertEquals(0, dao.getUnitCount(SOLDIER_UNIT_TYPE_ID, hospitalId));

        // Assert hospital map marker.
        mapMarker = dao.getMapMarkerByBuildingId(hospitalId);
        assertFalse(mapMarker.isReady());
        assertEquals((Integer) 0, mapMarker.getPendingProductionCount());
        assertEquals((Integer) 0, mapMarker.getCompletedProductionCount());

        // Assert that potential production buildings have turned green.
        // TODO
    }
}