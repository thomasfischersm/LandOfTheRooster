package com.playposse.landoftherooster.contentprovider.business.data;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * A test for {@link UnitTypeRepository}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UnitTypeRepositoryTest extends AbstractBusinessTest {

    @Test
    public void get_peasant() {
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
        UnitType unitType = unitTypeRepository.getUnitType(1);
        assertNotNull(unitType);
        assertEquals(1, unitType.getId());
        assertEquals(1, unitType.getCarryingCapacity());
    }

    @Test
    public void get_soldier() {
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
        UnitType unitType = unitTypeRepository.getUnitType(2);
        assertNotNull(unitType);
        assertEquals(2, unitType.getId());
        assertEquals(0, unitType.getCarryingCapacity());
    }

    @Test
    public void get_nonexistent() {
        UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
        UnitType unitType = unitTypeRepository.getUnitType(-1);
        assertNull(unitType);
    }
}