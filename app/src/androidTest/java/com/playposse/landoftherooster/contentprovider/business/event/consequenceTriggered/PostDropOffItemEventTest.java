package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * An instrumented test for scenarios that are started by {@link PostDropOffItemEvent}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PostDropOffItemEventTest extends AbstractBusinessTest {

    @Test
    public void triggerEvent_UserDropsOffItemEvent() {
        // Create building.
        long buildingId = createMillAndMarker(dao);

        // Drop off an unrelated resource.
        businessEngine.triggerEvent(
                new PostDropOffItemEvent(buildingId, new ResourceItem(1)));
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNull(buildingWithType.getBuilding().getProductionStart());

        // Drop off prerequisite.
        dao.insert(new Resource(WHEAT_RESOURCE_TYPE_ID, 1, buildingId));
        businessEngine.triggerEvent(
                new PostDropOffItemEvent(buildingId, new ResourceItem(WHEAT_RESOURCE_TYPE_ID)));
        buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        assertNotNull(buildingWithType.getBuilding().getProductionStart());
    }
}
