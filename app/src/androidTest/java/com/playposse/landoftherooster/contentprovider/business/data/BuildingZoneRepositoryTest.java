package com.playposse.landoftherooster.contentprovider.business.data;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * A test for {@link BuildingZoneRepository}
 */
public class BuildingZoneRepositoryTest extends AbstractBusinessTest {

    private BuildingZoneRepository repository;

    @Before
    public void setUp2() {
        // Create a new instance instead of getting the singleton. The background thread of the
        // game may mess with the state of the singleton and could make the test flaky.
        repository = new BuildingZoneRepository(dao);
    }

    @Test
    public void updateLocation() {
        // Create wheat field.
        long wheatFieldId = createWheatField(dao);
        Building building = dao.getBuildingById(wheatFieldId);

        // Assert initial state.
        assertNull(repository.getCurrentBuildingWithType());
        assertNull(repository.getCurrentLatLng());

        // Move to building.
        repository.updateLocation(DEFAULT_BUILDING_LATLNG);

        assertNotNull(repository.getCurrentBuildingWithType());
        assertEquals(wheatFieldId, repository.getCurrentBuildingWithType().getBuilding().getId());
        assertNotNull(repository.getCurrentLatLng());
        assertEquals(DEFAULT_BUILDING_LATLNG, repository.getCurrentLatLng());

        // Move to nowhere.
        repository.updateLocation(NOWHERE_LATLNG);

        assertNull(repository.getCurrentBuildingWithType());
        assertNotNull(repository.getCurrentLatLng());
        assertEquals(NOWHERE_LATLNG, repository.getCurrentLatLng());
    }
}
