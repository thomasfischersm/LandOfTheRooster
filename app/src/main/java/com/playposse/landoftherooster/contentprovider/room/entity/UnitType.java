package com.playposse.landoftherooster.contentprovider.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/**
 * A room entity that describes the ability of units.
 */
@Entity(tableName = "unit_type",
        indices = {
                @Index("precursor_resource_type_id"),
                @Index("precursor_unit_type_id")},
        foreignKeys = {
                @ForeignKey(entity = ResourceType.class,
                        parentColumns = "id",
                        childColumns = "precursor_resource_type_id",
                        onDelete = NO_ACTION),
                @ForeignKey(entity = UnitType.class,
                        parentColumns = "id",
                        childColumns = "precursor_unit_type_id",
                        onDelete = NO_ACTION)})
public class UnitType {

    @PrimaryKey(autoGenerate = false)
    private int id;
    private String name;

    @ColumnInfo(name = "carrying_capacity")
    private int carryingCapacity;

    private int attack;
    private int defense;
    private int damage;
    private int armor;
    private int health;

    @ColumnInfo(name = "precursor_resource_type_id")
    private Integer precursorResourceTypeId;

    @ColumnInfo(name = "precursor_unit_type_id")
    private Integer precursorUnitTypeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(int carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Integer getPrecursorResourceTypeId() {
        return precursorResourceTypeId;
    }

    public void setPrecursorResourceTypeId(Integer precursorResourceTypeId) {
        this.precursorResourceTypeId = precursorResourceTypeId;
    }

    public Integer getPrecursorUnitTypeId() {
        return precursorUnitTypeId;
    }

    public void setPrecursorUnitTypeId(Integer precursorUnitTypeId) {
        this.precursorUnitTypeId = precursorUnitTypeId;
    }
}
