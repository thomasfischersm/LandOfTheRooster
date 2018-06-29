package com.playposse.landoftherooster.services.combat;

import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.util.MinMaxRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of units that fight each other within a battle. For the simulation of a battle, units
 * will pair up and duel each other. If one side has more units the extra units may join these
 * duels. Because a person can only defend against one person, these extra units are guaranteed
 * a successful attack.
 *
 * <p>When a unit dies, all the battle groups are shuffled. This prevents uneven pairings from
 * happening.
 */
class BattleGroup {

    private List<UnitWithType> friendUnits = new ArrayList<>();
    private List<UnitWithType> enemyUnits = new ArrayList<>();

    BattleGroup(UnitWithType friendUnit, UnitWithType enemyUnit) {
        friendUnits.add(friendUnit);
        enemyUnits.add(enemyUnit);
    }

    void addFriendUnit(UnitWithType friendUnit) {
        friendUnits.add(friendUnit);
    }

    void addEnemyUnit(UnitWithType enemyUnit) {
        enemyUnits.add(enemyUnit);
    }

    boolean hasDeadUnit() {
        for (UnitWithType unitWithType : friendUnits) {
            if (unitWithType.getUnit().getHealth() <= 0) {
                return true;
            }
        }

        for (UnitWithType unitWithType : enemyUnits) {
            if (unitWithType.getUnit().getHealth() <= 0) {
                return true;
            }
        }

        return false;
    }

    List<BattleEventParcelable> fight() {
        List<BattleEventParcelable> events = new ArrayList<>();

        for (int i = 0; i < friendUnits.size(); i++) {
            int j = i % enemyUnits.size();
            boolean canDefend = (i == j);
            UnitWithType attacker = friendUnits.get(i);
            UnitWithType defender = enemyUnits.get(j);
            if (defender.getUnit().getHealth() > 0) {
                events.add(fight(attacker, defender, canDefend, true));
            }
        }

        for (int i = 0; i < enemyUnits.size(); i++) {
            int j = i % friendUnits.size();
            boolean canDefend = (i == j);
            UnitWithType attacker = enemyUnits.get(i);
            UnitWithType defender = friendUnits.get(j);
            if (defender.getUnit().getHealth() > 0) {
                events.add(fight(attacker, defender, canDefend, false));
            }
        }
        return events;
    }

    private BattleEventParcelable fight(
            UnitWithType attacker,
            UnitWithType defender,
            boolean canDefend,
            boolean isFriendAttack) {

        MinMaxRandom rand = Battle.getRandom();

        int attackerVeteranLevel = Math.min(attacker.getUnit().getVeteranLevel(), attacker.getType().getAttack());
        int defenderVeteranLevel = defender.getUnit().getVeteranLevel();

        // Decide fight success.
        int maxAttack = attacker.getType().getAttack() + 1;
        int minAttack =
                Math.min(attacker.getUnit().getVeteranLevel(), attacker.getType().getAttack());
        int attack = rand.nextInt(minAttack, maxAttack);
        int maxDefense = defender.getType().getDefense() + 1;
        int minDefense =
                Math.min(defender.getUnit().getVeteranLevel(), defender.getType().getDefense());
        int defense = rand.nextInt(minDefense, maxDefense);

        if (!canDefend || (attack > defense)) {
            // Attack was succeeded. Calculate damage.
            int maxDamage = attacker.getType().getDamage() + 1;
            int minDamage = Math.min(
                    attacker.getUnit().getVeteranLevel(),
                    attacker.getType().getDamage());
            int damage = rand.nextInt(minDamage, maxDamage);
            int armor = defender.getType().getArmor();
            int actualDamage = Math.max(0, damage - armor);
            int previousHealth = defender.getUnit().getHealth();
            defender.getUnit().setHealth(Math.max(0, previousHealth - actualDamage));
            return new BattleEventParcelable(
                    isFriendAttack,
                    attacker,
                    defender,
                    attack,
                    defense,
                    damage,
                    armor,
                    actualDamage,
                    defender.getUnit().getHealth());
        } else {
            return new BattleEventParcelable(
                    isFriendAttack,
                    attacker,
                    defender,
                    attack,
                    defense,
                    0,
                    0,
                    0,
                    defender.getUnit().getHealth());
        }
    }
}
