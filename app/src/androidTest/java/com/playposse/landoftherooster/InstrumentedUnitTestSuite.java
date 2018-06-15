package com.playposse.landoftherooster;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCacheTest;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngineTest;
import com.playposse.landoftherooster.contentprovider.business.action.DropOffItemActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.ExecuteBattleActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.OpenBuildingDialogsActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.PickUpItemActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.PickUpUnitFromHospitalActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.RespawnBattleBuildingActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.UpdateBattleBuildingMarkerActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.UpdateProductionBuildingMarkerActionTest;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingZoneRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.LocationUpdateEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteHealingEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteProductionEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AdmitUnitToHospitalEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AssignPeasantEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.InitiateBattleEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEventTest;
import com.playposse.landoftherooster.contentprovider.business.initializer.FreeProductionStartupInitializerTest;
import com.playposse.landoftherooster.contentprovider.business.initializer.HealingStartupInitializerTest;
import com.playposse.landoftherooster.contentprovider.business.initializer.ProductionStartupInitializerTest;
import com.playposse.landoftherooster.contentprovider.business.initializer.RespawnBattleStartupInitializerTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.AdmitUnitToHospitalPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.AssignPeasantPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.DropOffItemPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.ExecuteBattlePreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.InitiateHealingPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpItemPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpUnitFromHospitalPreconditionTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.RespawnBattleBuildingPreconditionTest;
import com.playposse.landoftherooster.contentprovider.room.RoosterDaoTest;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suite for all instrumented tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AdmitUnitToHospitalEventTest.class,
        AdmitUnitToHospitalPreconditionTest.class,
        AssignPeasantEventTest.class,
        AssignPeasantPreconditionTest.class,
        BuildingDiscoveryRepositoryTest.class,
        BuildingZoneRepositoryTest.class,
        BusinessDataCacheTest.class,
        BusinessEngineTest.class,
        BuildingRepositoryTest.class,
        CompleteHealingEventTest.class,
        CompleteProductionEventTest.class,
        DaoEventRegistryTest.class,
        DropOffItemActionTest.class,
        DropOffItemEventTest.class,
        DropOffItemPreconditionTest.class,
        ExecuteBattleActionTest.class,
        ExecuteBattlePreconditionTest.class,
        FreeProductionStartupInitializerTest.class,
        HealingStartupInitializerTest.class,
        InitiateBattleEventTest.class,
        InitiateHealingPreconditionTest.class,
        LocationUpdateEventTest.class,
        OpenBuildingDialogsActionTest.class,
        PickUpItemActionTest.class,
        PickUpItemPreconditionTest.class,
        PickUpUnitFromHospitalActionTest.class,
        PickUpUnitFromHospitalEventTest.class,
        PickUpUnitFromHospitalPreconditionTest.class,
        PostDropOffItemEventTest.class,
        ProductionRuleRepositoryTest.class,
        ProductionStartupInitializerTest.class,
        RespawnBattleBuildingActionTest.class,
        RespawnBattleBuildingPreconditionTest.class,
        RespawnBattleStartupInitializerTest.class,
        RoosterDaoTest.class,
        UnitTypeRepositoryTest.class,
        UpdateBattleBuildingMarkerActionTest.class,
        UpdateProductionBuildingMarkerActionTest.class
})
public class InstrumentedUnitTestSuite {
}
