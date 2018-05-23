package com.playposse.landoftherooster.contentprovider.business.data;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.contentprovider.business.AbstractBusinessTest;
import com.playposse.landoftherooster.contentprovider.business.ResourceItem;
import com.playposse.landoftherooster.contentprovider.business.UnitItem;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * A test for {@link ProductionRuleRepository}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProductionRuleRepositoryTest extends AbstractBusinessTest {

    private ProductionRuleRepository productionRuleRepository;

    @Before
    public void setUp2() {
        productionRuleRepository = ProductionRuleRepository.get(dao);
    }

    @Test
    public void getBuildingTypeIds() {

    }

    @Test
    public void getBuildingsWithAffectedProductionRules_ResourceItem_noBuilding() {
        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(wheatItem);

        assertEquals(0, buildingWithTypes.size());
    }

    @Test
    public void getBuildingsWithAffectedProductionRules_ResourceItem_oneBuilding() {
        // Create a wheat field.
        long wheatFieldId =createWheatField(dao);

        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(wheatItem);

        assertEquals(1, buildingWithTypes.size());
        assertBuildingIds(buildingWithTypes, wheatFieldId);
    }

    @Test
    public void getBuildingsWithAffectedProductionRules_ResourceItem_twoBuildings() {
        // Create a wheat field.
        long wheatFieldId = createWheatField(dao);

        // Create mill.
        Building mill = new Building(MILL_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        long millId = dao.insert(mill);

        ResourceItem wheatItem = new ResourceItem(WHEAT_RESOURCE_TYPE_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(wheatItem);

        assertEquals(2, buildingWithTypes.size());
        assertBuildingIds(buildingWithTypes, wheatFieldId, millId);
    }

    @Test
    public void getBuildingsWithAffectedProductionRules_UnitItem_noBuilding() {
        UnitItem peasantItem = new UnitItem(GameConfig.PEASANT_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(peasantItem);

        assertEquals(0, buildingWithTypes.size());
    }

    @Test
    public void getBuildingsWithAffectedProductionRules_UnitItem_oneBuilding() {
        // Create a village.
        Building village = new Building(VILLAGE_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        long villageId = dao.insert(village);

        UnitItem peasantItem = new UnitItem(GameConfig.PEASANT_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(peasantItem);

        assertEquals(1, buildingWithTypes.size());
        assertBuildingIds(buildingWithTypes, villageId);
    }

    @Test
    public void getBuildingsWithAffectedProductionRules_UnitItem_twoBuildings() {
        // Create a village.
        Building village = new Building(VILLAGE_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        long villageId = dao.insert(village);

        // Create a barrack.
        Building barrack = new Building(BARRACKS_BUILDING_TYPE_ID, LATITUDE, LONGITUDE);
        long barrackId = dao.insert(barrack);

        UnitItem peasantItem = new UnitItem(GameConfig.PEASANT_ID);
        List<BuildingWithType> buildingWithTypes =
                productionRuleRepository.getBuildingsWithAffectedProductionRules(peasantItem);

        assertEquals(2, buildingWithTypes.size());
        assertBuildingIds(buildingWithTypes, villageId, barrackId);
    }

    @Test
    public void getProductionRulesByBuildingTypeId_wheatField() {
        List<ProductionRule> productionRules =
                productionRuleRepository.getProductionRulesByBuildingTypeId(
                        WHEAT_FIELD_BUILDING_TYPE_ID);

        assertEquals(1, productionRules.size());

        ProductionRule productionRule = productionRules.get(0);
        assertEquals(WHEAT_FIELD_BUILDING_TYPE_ID, productionRule.getBuildingTypeId());
        assertNull(productionRule.getInputResourceTypeIds());
        assertEquals((Long) WHEAT_RESOURCE_TYPE_ID, productionRule.getOutputResourceTypeId());
        assertNull(productionRule.getOutputUnitTypeId());
    }
}
