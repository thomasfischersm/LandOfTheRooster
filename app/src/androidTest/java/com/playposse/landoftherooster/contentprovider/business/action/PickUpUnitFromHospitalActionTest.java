package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepository;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpUnitFromHospitalPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNull;

/**
 * A test for {@link PickUpUnitFromHospitalAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickUpUnitFromHospitalActionTest extends AbstractBusinessTest {

    @Test
    public void perform() {
        // Create hospital.
        long hospitalId = createHospitalAndMarker(dao);

        // Create recoveredUnit.
        List<Unit> units = createUnits(dao, 1, SOLDIER_UNIT_TYPE_ID, hospitalId);
        Unit unit = units.get(0);
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
        UnitWithType unitWithType = unitTypeRepository.getUnitWithType(unit);

        // Test action.
        PickUpUnitFromHospitalEvent event = new PickUpUnitFromHospitalEvent(hospitalId);
        BusinessDataCache cache = new BusinessDataCache(dao, hospitalId);
        PickUpUnitFromHospitalPreconditionOutcome outcome =
                new PickUpUnitFromHospitalPreconditionOutcome(true, unitWithType);
        PickUpUnitFromHospitalAction action = new PickUpUnitFromHospitalAction();
        action.perform(event, outcome, cache);

        // Assert result.
        UnitWithType resultUnitWithType = unitTypeRepository.queryUnitWithTypeById(unit.getId());
        assertNull(resultUnitWithType.getUnit().getLocatedAtBuildingId());
    }
}
