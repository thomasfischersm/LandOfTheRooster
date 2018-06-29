package com.playposse.landoftherooster.services.combat;

import android.util.Log;

import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.UnitInjuredEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;
import com.playposse.landoftherooster.util.MinMaxRandom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playposse.landoftherooster.GameConfig.DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT;

/**
 * A business object that represents and simulated a single battle.
 */
public class Battle {

    private static final String LOG_TAG = Battle.class.getSimpleName();

    private final BuildingWithType buildingWithType;
    private final RoosterDao dao;

    private List<UnitWithType> friendUnits;
    private List<UnitWithType> enemyUnits;
    private List<BattleGroup> battleGroups = new ArrayList<>();
    private List<BattleEventParcelable> events = new ArrayList<>();

    private static final MinMaxRandom random = new MinMaxRandom();

    public Battle(RoosterDao dao, BuildingWithType buildingWithType) {
        this.buildingWithType = buildingWithType;
        this.dao = dao;

        loadData();
    }

    private void loadData() {
        // Load friend units.
        friendUnits = dao.getUnitsWithTypeJoiningUser();

        // Load enemy units.
        UnitType enemyUnitType =
                dao.getUnitTypeById(buildingWithType.getBuildingType().getEnemyUnitTypeId());
        enemyUnits = new ArrayList<>();
        for (int i = 0; i < buildingWithType.getBuildingType().getEnemyUnitCount(); i++) {
            Unit enemyUnit = new Unit();
            enemyUnit.setUnitTypeId(enemyUnitType.getId());
            enemyUnit.setHealth(enemyUnitType.getHealth());

            UnitWithType enemyUnitWithType = new UnitWithType();
            enemyUnitWithType.setUnit(enemyUnit);
            enemyUnitWithType.setType(enemyUnitType);
            enemyUnits.add(enemyUnitWithType);
        }
    }

    private List<UnitWithType> getAliveFriendUnits() {
        List<UnitWithType> aliveUnits = new ArrayList<>();
        for (UnitWithType unitWithType : friendUnits) {
            if (unitWithType.getUnit().getHealth() > 0) {
                aliveUnits.add(unitWithType);
            }
        }
        return aliveUnits;
    }

    private List<UnitWithType> getAliveEnemyUnits() {
        List<UnitWithType> aliveUnits = new ArrayList<>();
        for (UnitWithType unitWithType : enemyUnits) {
            if (unitWithType.getUnit().getHealth() > 0) {
                aliveUnits.add(unitWithType);
            }
        }
        return aliveUnits;
    }

    private void createBattleGroups() {
        battleGroups.clear();

        List<UnitWithType> aliveFriendUnits = getAliveFriendUnits();
        List<UnitWithType> aliveEnemyUnits = getAliveEnemyUnits();

        int min = Math.min(aliveFriendUnits.size(), aliveEnemyUnits.size());
        int max = Math.max(aliveFriendUnits.size(), aliveEnemyUnits.size());

        // Exit early if one side has been decimated.
        if (min == 0) {
            return;
        }

        // Add unit pairings.
        for (int i = 0; i < min; i++) {
            battleGroups.add(new BattleGroup(aliveFriendUnits.get(i), aliveEnemyUnits.get(i)));
        }

        // Add additional units to the pairings from the side that has more units.
        if (min == max) {
            // Both sides have the same amount of units. Exit early.
            return;
        }

        if (aliveFriendUnits.size() > aliveEnemyUnits.size()) {
            for (int i = min; i < max; i++) {
                battleGroups.get(i % min).addFriendUnit(aliveFriendUnits.get(i));
            }
        } else {
            for (int i = min; i < max; i++) {
                battleGroups.get(i % min).addEnemyUnit(aliveEnemyUnits.get(i));
            }
        }
    }

    /**
     * Checks if any unit has died in a group. If this happens, the groups will get re-balanced.
     */
    private boolean hasDeathInAGroup() {
        for (BattleGroup battleGroup : battleGroups) {
            if (battleGroup.hasDeadUnit()) {
                return true;
            }
        }
        return false;
    }

    public BattleSummaryParcelable fight() {
        Log.d(LOG_TAG, "fight: Starting battle");

        List<UnitWithType> startingAliveFriendUnits = getAliveFriendUnits();
        int startingFriendUnitCount = startingAliveFriendUnits.size();
        int startingFriendHealth = getCumulativeHealth(startingAliveFriendUnits);
        List<UnitWithType> startingAliveEnemyUnits = getAliveEnemyUnits();
        int startingEnemyUnitCount = startingAliveEnemyUnits.size();
        int startingEnemyHealth = getCumulativeHealth(startingAliveEnemyUnits);

        do {
            fightRound();
        } while (battleGroups.size() > 0);

        List<UnitWithType> aliveFriendUnits = getAliveFriendUnits();
        List<UnitWithType> aliveEnemyUnits = getAliveEnemyUnits();

        boolean hasFriendWon = aliveFriendUnits.size() > 0;

        // Store outcome of the battle to the database.
        saveUnits();
        if (hasFriendWon) {
            saveBuilding();
            saveConquestPrize();
        }

        int endingFriendHealth = getCumulativeHealth(aliveFriendUnits);
        int endingEnemyHealth = getCumulativeHealth(aliveEnemyUnits);
        int friendlyHealthLost = startingFriendHealth - endingFriendHealth;

        if (friendlyHealthLost > 0) {
            BusinessEngine.get()
                    .triggerEvent(new UnitInjuredEvent());
        }

        return new BattleSummaryParcelable(
                hasFriendWon,
                startingFriendUnitCount - aliveFriendUnits.size(),
                friendlyHealthLost,
                startingEnemyUnitCount - aliveEnemyUnits.size(),
                startingEnemyHealth - endingEnemyHealth,
                events);
    }

    private void fightRound() {
        Log.d(LOG_TAG, "fightRound: Starting fight round.");

        // Re-create battle groups if necessary
        if ((battleGroups.size() == 0) || (hasDeathInAGroup())) {
            createBattleGroups();
        }

        for (BattleGroup battleGroup : battleGroups) {
            List<BattleEventParcelable> battleEvents = battleGroup.fight();
            events.addAll(battleEvents);
        }
    }

    private void saveUnits() {
        for (UnitWithType unitWithType : friendUnits) {
            Unit unit = unitWithType.getUnit();
            if (unit.getHealth() <= 0) {
                // Delete unit.
                DaoEventRegistry.get(dao).delete(unit);
            } else {
                unitWithType.incrementVeteranLevel();

                // Save injury.
                DaoEventRegistry.get(dao).update(unit);
            }
        }
    }

    private void saveBuilding() {
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(new Date());
        DaoEventRegistry.get(dao).update(building);
    }

    private void saveConquestPrize() {
        Integer conquestPrizeResourceTypeId =
                buildingWithType.getBuildingType().getConquestPrizeResourceTypeId();
        ResourceType conquestPrizeResourceType = dao.getResourceTypeById(conquestPrizeResourceTypeId);
        RoosterDaoUtil.creditResource(
                dao,
                conquestPrizeResourceType.getId(),                DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT,
                null);
    }

    private int getCumulativeHealth(List<UnitWithType> unitWithTypes) {
        int health = 0;

        if (unitWithTypes != null) {
            for (UnitWithType unitWithType : unitWithTypes) {
                health += unitWithType.getUnit().getHealth();
            }
        }

        return health;
    }

    public static MinMaxRandom getRandom() {
        return random;
    }
}
