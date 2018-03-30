package com.playposse.landoftherooster.contentprovider.parser;

/**
 * A GSON class to read unit types.
 */
public class UnitType {

    private int id;
    private String name;
    private int carryingCapacity;
    private int attack;
    private int defense;
    private int armor;
    private int health;
    private Integer precursorResourceTypeId;
    private Integer precursorUnitTypeId;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCarryingCapacity() {
        return carryingCapacity;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getArmor() {
        return armor;
    }

    public int getHealth() {
        return health;
    }

    public Integer getPrecursorResourceTypeId() {
        return precursorResourceTypeId;
    }

    public Integer getPrecursorUnitTypeId() {
        return precursorUnitTypeId;
    }
}
