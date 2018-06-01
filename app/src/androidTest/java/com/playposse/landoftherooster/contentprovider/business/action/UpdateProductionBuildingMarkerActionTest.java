package com.playposse.landoftherooster.contentprovider.business.action;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.precondition.UpdateProductionBuildingMarkerPreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link UpdateProductionBuildingMarkerAction}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UpdateProductionBuildingMarkerActionTest extends AbstractBusinessTest {

    private final UpdateProductionBuildingMarkerAction action = new UpdateProductionBuildingMarkerAction();

    @Test
    public void perform_noBuildings() {
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);

        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        new ArrayList<BuildingWithType>());
        action.perform(null, outcome, cache);
    }

    @Test
    public void perform_wheatReadyForPickup_change() {
        // Create test data: 1 wheat field with mark and 1 wheat.
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        long wheatFieldId = createWheatFieldAndMarker(dao);
        BuildingWithType wheatField = dao.getBuildingWithTypeByBuildingId(wheatFieldId);
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);
        Date lastModified = getLastModifiedForMarker(dao, wheatFieldId);

        // Execute test.
        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        Collections.singletonList(wheatField));
        action.perform(null, outcome, cache);

        // Assert test result.
        List<MapMarker> markers =
                dao.getMapMarkerByBuildingIds(Collections.singletonList(wheatFieldId));
        assertEquals(1, markers.size());

        MapMarker marker = markers.get(0);
        assertEquals((Long) wheatFieldId, marker.getBuildingId());
        assertEquals((Long) WHEAT_FIELD_BUILDING_TYPE_ID, marker.getBuildingTypeId());
        assertTrue(marker.isReady());
        assertEquals((Integer) 0, marker.getPendingProductionCount());
        assertEquals((Integer) 1, marker.getCompletedProductionCount());
        assertNotSame(lastModified.getTime(), marker.getLastModified().getTime());
    }

    @Test
    public void perform_wheatReadyForPickup_noChange() {
        // Create test data: 1 wheat field with mark and 1 wheat.
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        long wheatFieldId = createWheatFieldAndMarker(dao);
        BuildingWithType wheatField = dao.getBuildingWithTypeByBuildingId(wheatFieldId);
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, wheatFieldId);
        Date lastModified = getLastModifiedForMarker(dao, wheatFieldId);

        // Set map marker to already expected state
        List<MapMarker> originalMarkers =
                dao.getMapMarkerByBuildingIds(Collections.singletonList(wheatFieldId));
        MapMarker originalMarker = originalMarkers.get(0);
        originalMarker.setPendingProductionCount(0);
        originalMarker.setCompletedProductionCount(1);
        originalMarker.setReady(true);
        dao.update(originalMarker);

        // Execute test.
        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        Collections.singletonList(wheatField));
        action.perform(null, outcome, cache);

        // Assert test result.
        List<MapMarker> markers =
                dao.getMapMarkerByBuildingIds(Collections.singletonList(wheatFieldId));
        assertEquals(1, markers.size());

        MapMarker marker = markers.get(0);
        assertEquals((Long) wheatFieldId, marker.getBuildingId());
        assertEquals((Long) WHEAT_FIELD_BUILDING_TYPE_ID, marker.getBuildingTypeId());
        assertTrue(marker.isReady());
        assertEquals((Integer) 0, marker.getPendingProductionCount());
        assertEquals((Integer) 1, marker.getCompletedProductionCount());
        assertEquals(lastModified.getTime(), marker.getLastModified().getTime());
    }

    @Test
    public void perform_wheatReadyForProduction_singleItemPending() {
        // Create test data: 1 mill with mark and 1 wheat.
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        long millId = createMillAndMarker(dao);
        BuildingWithType mill = dao.getBuildingWithTypeByBuildingId(millId);
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 1, millId);
        Date lastModified = getLastModifiedForMarker(dao, millId);

        // Execute test.
        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        Collections.singletonList(mill));
        action.perform(null, outcome, cache);

        // Assert test result.
        List<MapMarker> markers = dao.getMapMarkerByBuildingIds(Collections.singletonList(millId));
        assertEquals(1, markers.size());

        MapMarker marker = markers.get(0);
        assertEquals((Long) millId, marker.getBuildingId());
        assertEquals((Long) MILL_BUILDING_TYPE_ID, marker.getBuildingTypeId());
        assertFalse(marker.isReady());
        assertEquals((Integer) 1, marker.getPendingProductionCount());
        assertEquals((Integer) 0, marker.getCompletedProductionCount());
        assertNotSame(lastModified.getTime(), marker.getLastModified().getTime());
    }

    @Test
    public void perform_wheatReadyForProduction_threeItemsPending() {
        // Create test data: 1 mill with mark and 3 wheat.
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        long millId = createMillAndMarker(dao);
        BuildingWithType mill = dao.getBuildingWithTypeByBuildingId(millId);
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 3, millId);
        Date lastModified = getLastModifiedForMarker(dao, millId);

        // Execute test.
        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        Collections.singletonList(mill));
        action.perform(null, outcome, cache);

        // Assert test result.
        List<MapMarker> markers = dao.getMapMarkerByBuildingIds(Collections.singletonList(millId));
        assertEquals(1, markers.size());

        MapMarker marker = markers.get(0);
        assertEquals((Long) millId, marker.getBuildingId());
        assertEquals((Long) MILL_BUILDING_TYPE_ID, marker.getBuildingTypeId());
        assertFalse(marker.isReady());
        assertEquals((Integer) 3, marker.getPendingProductionCount());
        assertEquals((Integer) 0, marker.getCompletedProductionCount());
        assertNotSame(lastModified.getTime(), marker.getLastModified().getTime());
    }

    @Test
    public void perform_wheatReadyForDropOff() {
        // Create test data: 1 mill with mark and add 3 wheat to the user.
        BusinessDataCache cache = new BusinessDataCache(dao);
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        long millId = createMillAndMarker(dao);
        BuildingWithType mill = dao.getBuildingWithTypeByBuildingId(millId);
        RoosterDaoUtil.creditResource(dao, WHEAT_RESOURCE_TYPE_ID, 3, null);
        Date lastModified = getLastModifiedForMarker(dao, millId);

        // Execute test.
        UpdateProductionBuildingMarkerPreconditionOutcome outcome =
                new UpdateProductionBuildingMarkerPreconditionOutcome(
                        true,
                        wheatItem,
                        Collections.singletonList(mill));
        action.perform(null, outcome, cache);

        // Assert test result.
        List<MapMarker> markers = dao.getMapMarkerByBuildingIds(Collections.singletonList(millId));
        assertEquals(1, markers.size());

        MapMarker marker = markers.get(0);
        assertEquals((Long) millId, marker.getBuildingId());
        assertEquals((Long) MILL_BUILDING_TYPE_ID, marker.getBuildingTypeId());
        assertTrue(marker.isReady());
        assertEquals((Integer) 0, marker.getPendingProductionCount());
        assertEquals((Integer) 0, marker.getCompletedProductionCount());
        assertNotSame(lastModified.getTime(), marker.getLastModified().getTime());
    }
}
