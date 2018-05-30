package com.playposse.landoftherooster.contentprovider.business.precondition;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * A test for {@link UpdateProductionBuildingMarkerPrecondition}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UpdateProductionBuildingMarkerPreconditionTest extends AbstractBusinessTest {

    private final UpdateProductionBuildingMarkerPrecondition precondition =
            new UpdateProductionBuildingMarkerPrecondition();

    @Test
    public void evaluate_UserPicksUpItemEvent_noBuildings() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostPickUpItemEvent event = new PostPickUpItemEvent(1L, wheatItem);

        evaluate_noBuildings(event);
    }

    @Test
    public void evaluate_UserPicksUpItemEvent_oneWheatField() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostPickUpItemEvent event = new PostPickUpItemEvent(1L, wheatItem);

        evaluate_oneWheatField(event);
    }

    @Test
    public void evaluate_UserPicksUpItemEvent_twoWheatFields() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostPickUpItemEvent event = new PostPickUpItemEvent(1L, wheatItem);

        evaluate_twoWheatFields(event);
    }

    @Test
    public void evaluate_UserPicksUpItemEvent_wheatFieldAndMill() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostPickUpItemEvent event = new PostPickUpItemEvent(1L, wheatItem);

        evaluate_wheatFieldAndMill(event);
    }

    @Test
    public void evaluate_UserDropsOffItemEvent() {
        PostDropOffItemEvent event =
                PostDropOffItemEvent.createForResource(1L, WHEAT_RESOURCE_TYPE_ID);

        evaluate_wheatFieldAndMill(event);
    }

    @Test
    public void evaluate_ItemProductionSucceededEvent() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostCompleteProductionEvent event =
                new PostCompleteProductionEvent(1L, wheatItem);

        evaluate_wheatFieldAndMill(event);
    }

    @Test
    public void evaluate_FreeItemProductionSucceededEvent() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        PostCompleteFreeProductionEvent event =
                new PostCompleteFreeProductionEvent(1L, wheatItem);

        evaluate_wheatFieldAndMill(event);
    }

    private void evaluate_noBuildings(BusinessEvent event) {
        BusinessDataCache cache = new BusinessDataCache(dao, 1L);

        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        assertFalse(outcome.getSuccess());
        assertTrue(outcome instanceof UpdateProductionBuildingMarkerPreconditionOutcome);

        UpdateProductionBuildingMarkerPreconditionOutcome castOutcome =
                (UpdateProductionBuildingMarkerPreconditionOutcome) outcome;
        assertNull(castOutcome.getAffectedBuildingWithTypes());
    }

    private void evaluate_oneWheatField(BusinessEvent event) {
        // Create wheat field.
        long wheatFieldId = createWheatField(dao);
        BusinessDataCache cache = new BusinessDataCache(dao, 1L);

        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        assertTrue(outcome.getSuccess());
        assertTrue(outcome instanceof UpdateProductionBuildingMarkerPreconditionOutcome);

        UpdateProductionBuildingMarkerPreconditionOutcome castOutcome =
                (UpdateProductionBuildingMarkerPreconditionOutcome) outcome;
        List<BuildingWithType> buildingWithTypes = castOutcome.getAffectedBuildingWithTypes();
        assertEquals(1, buildingWithTypes.size());
        assertEquals(wheatFieldId, buildingWithTypes.get(0).getBuilding().getId());
        assertEquals(
                WHEAT_FIELD_BUILDING_TYPE_ID,
                buildingWithTypes.get(0).getBuilding().getBuildingTypeId());
    }


    private void evaluate_twoWheatFields(BusinessEvent event) {
        long wheatFieldId0 = createWheatField(dao);
        long wheatFieldId1 = createWheatField(dao);
        BusinessDataCache cache = new BusinessDataCache(dao, 1L);

        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        assertTrue(outcome.getSuccess());
        assertTrue(outcome instanceof UpdateProductionBuildingMarkerPreconditionOutcome);

        UpdateProductionBuildingMarkerPreconditionOutcome castOutcome =
                (UpdateProductionBuildingMarkerPreconditionOutcome) outcome;
        List<BuildingWithType> buildingWithTypes = castOutcome.getAffectedBuildingWithTypes();
        assertEquals(2, buildingWithTypes.size());
        assertEquals(
                WHEAT_FIELD_BUILDING_TYPE_ID,
                buildingWithTypes.get(0).getBuilding().getBuildingTypeId());
        assertEquals(
                WHEAT_FIELD_BUILDING_TYPE_ID,
                buildingWithTypes.get(1).getBuilding().getBuildingTypeId());
    }

    private void evaluate_wheatFieldAndMill(BusinessEvent event) {
        long wheatFieldId = createWheatField(dao);
        long millId = createMill(dao);
        createBakery(dao);

        BusinessDataCache cache = new BusinessDataCache(dao, 1L);

        PreconditionOutcome outcome = precondition.evaluate(event, cache);

        assertTrue(outcome.getSuccess());
        assertTrue(outcome instanceof UpdateProductionBuildingMarkerPreconditionOutcome);

        UpdateProductionBuildingMarkerPreconditionOutcome castOutcome =
                (UpdateProductionBuildingMarkerPreconditionOutcome) outcome;
        List<BuildingWithType> buildingWithTypes = castOutcome.getAffectedBuildingWithTypes();
        assertEquals(2, buildingWithTypes.size());
        assertBuildingIds(buildingWithTypes, wheatFieldId, millId);
        assertBuildingTypeIds(
                buildingWithTypes,
                WHEAT_FIELD_BUILDING_TYPE_ID,
                MILL_BUILDING_TYPE_ID);
    }
}
