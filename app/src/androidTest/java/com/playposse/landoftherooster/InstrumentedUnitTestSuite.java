package com.playposse.landoftherooster;

import com.playposse.landoftherooster.contentprovider.business.BusinessEngineTest;
import com.playposse.landoftherooster.contentprovider.business.action.ExecuteBattleActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.RespawnBattleBuildingActionTest;
import com.playposse.landoftherooster.contentprovider.business.action.UpdateBuildingMarkerActionTest;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingDiscoveryRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.ProductionRuleRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepositoryTest;
import com.playposse.landoftherooster.contentprovider.business.event.InitiateBattleEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionEndedEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.LocationUpdateEventTest;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEventTest;
import com.playposse.landoftherooster.contentprovider.business.precondition.ExecuteBattlePreconditionTest;
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
        BuildingDiscoveryRepositoryTest.class,
        BusinessEngineTest.class,
        DaoEventRegistryTest.class,
        ExecuteBattleActionTest.class,
        ExecuteBattlePreconditionTest.class,
        InitiateBattleEventTest.class,
        ItemProductionEndedEventTest.class,
        LocationUpdateEventTest.class,
        ProductionRuleRepositoryTest.class,
        RespawnBattleBuildingActionTest.class,
        RespawnBattleBuildingPreconditionTest.class,
        RoosterDaoTest.class,
        UnitTypeRepositoryTest.class,
        UpdateBuildingMarkerActionTest.class,
        UserDropsOffItemEventTest.class
})
public class InstrumentedUnitTestSuite {
}
