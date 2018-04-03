package com.playposse.landoftherooster.services.combat;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

/**
 * A record of a single attack inside of a battle. This may be shown to the user to create an idea
 * of what happened during the battle.
 */
public final class BattleEventParcelable implements Parcelable {

    private final boolean isFriendAttack;
    private final String attackerUnitName;
    private final String defenderUnitName;
    private final int attack;
    private final int defense;

    @Nullable
    private final Integer damage;

    @Nullable
    private final Integer armor;

    @Nullable
    private final Integer actualDamage;

    BattleEventParcelable(
            boolean isFriendAttack,
            UnitWithType attackerUnitWithType,
            UnitWithType defenderUnitWithType,
            int attack, int defense,
            @Nullable Integer damage,
            @Nullable Integer armor,
            @Nullable Integer actualDamage) {

        this.isFriendAttack = isFriendAttack;
        this.attackerUnitName = attackerUnitWithType.getType().getName();
        this.defenderUnitName = defenderUnitWithType.getType().getName();
        this.attack = attack;
        this.defense = defense;
        this.damage = damage;
        this.armor = armor;
        this.actualDamage = actualDamage;
    }

    private BattleEventParcelable(Parcel in) {
        isFriendAttack = in.readByte() != 0;
        attackerUnitName = in.readString();
        defenderUnitName = in.readString();
        attack = in.readInt();
        defense = in.readInt();
        damage = in.readInt();
        armor = in.readInt();
        actualDamage = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (isFriendAttack ? 1 : 0));
        out.writeString(attackerUnitName);
        out.writeString(defenderUnitName);
        out.writeInt(attack);
        out.writeInt(defense);
        out.writeInt(damage);
        out.writeInt(armor);
        out.writeInt(actualDamage);
    }

    public static final Parcelable.Creator<BattleEventParcelable> CREATOR
            = new Parcelable.Creator<BattleEventParcelable>() {
        @Override
        public BattleEventParcelable createFromParcel(Parcel in) {
            return new BattleEventParcelable(in);
        }

        @Override
        public BattleEventParcelable[] newArray(int size) {
            return new BattleEventParcelable[size];
        }
    };

    public boolean isFriendAttack() {
        return isFriendAttack;
    }

    public String getAttackerUnitName() {
        return attackerUnitName;
    }

    public String getDefenderUnitName() {
        return defenderUnitName;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    @Nullable
    public Integer getDamage() {
        return damage;
    }

    @Nullable
    public Integer getArmor() {
        return armor;
    }

    @Nullable
    public Integer getActualDamage() {
        return actualDamage;
    }
}
