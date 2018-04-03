package com.playposse.landoftherooster.services.combat;

import android.content.Context;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * A business object that represents and simulated a single battle.
 */
public class Battle {

    private static final int DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT = 1;

    private final Context context;
    private final BuildingWithType buildingWithType;
    private final RoosterDao dao;

    private List<UnitWithType> friendUnits;
    private List<UnitWithType> enemyUnits;
    private List<BattleGroup> battleGroups = new ArrayList<>();
    private List<BattleEventParcelable> events = new ArrayList<>();

    private static final Random random = new Random();

    public Battle(Context context, BuildingWithType buildingWithType) {
        this.context = context;
        this.buildingWithType = buildingWithType;

        dao = RoosterDatabase.getInstance(context).getDao();

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
                battleGroups.get(i % min).addFriendUnit(aliveEnemyUnits.get(i));
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

        return new BattleSummaryParcelable(hasFriendWon);
    }

    private void fightRound() {
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
                dao.deleteUnit(unit);
            } else {
                // Save injury.
                dao.update(unit);
            }
        }
    }

    private void saveBuilding() {
        Building building = buildingWithType.getBuilding();
        building.setLastConquest(new Date());
        dao.update(building);
    }

    private void saveConquestPrize() {
        Integer conquestPrizeResourceTypeId =
                buildingWithType.getBuildingType().getConquestPrizeResourceTypeId();
        ResourceType conquestPrizeResourceType = dao.getResourceTypeById(conquestPrizeResourceTypeId);
        RoosterDaoUtil.credit(
                context,
                conquestPrizeResourceType,
                DEFAULT_CONQUEST_PRIZE_RESOURCE_AMOUNT);
    }

    public static Random getRandom() {
        return random;
    }
}
