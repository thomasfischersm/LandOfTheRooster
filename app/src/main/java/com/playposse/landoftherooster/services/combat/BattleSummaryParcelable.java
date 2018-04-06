package com.playposse.landoftherooster.services.combat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class that describes the battle outcome.
 */
public class BattleSummaryParcelable implements Parcelable {

    private final boolean didFriendsWin;
    private final int friendlyUnitCountLost;
    private final int friendlyHealthLost;
    private final int enemyUnitCountLost;
    private final int enemyHealthLost;
    private final List<BattleEventParcelable> battleEvents;

    public BattleSummaryParcelable(
            boolean didFriendsWin,
            int friendlyUnitCountLost,
            int friendlyHealthLost,
            int enemyUnitCountLost,
            int enemyHealthLost,
            List<BattleEventParcelable> battleEvents) {

        this.didFriendsWin = didFriendsWin;
        this.friendlyUnitCountLost = friendlyUnitCountLost;
        this.friendlyHealthLost = friendlyHealthLost;
        this.enemyUnitCountLost = enemyUnitCountLost;
        this.enemyHealthLost = enemyHealthLost;
        this.battleEvents = battleEvents;
    }

    private BattleSummaryParcelable(Parcel in) {
        didFriendsWin = (in.readByte() != 0);
        friendlyUnitCountLost = in.readInt();
        friendlyHealthLost = in.readInt();
        enemyUnitCountLost = in.readInt();
        enemyHealthLost = in.readInt();

        battleEvents = new ArrayList<>();
        in.readList(battleEvents, null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (didFriendsWin ? 1 : 0));
        out.writeInt(friendlyUnitCountLost);
        out.writeInt(friendlyHealthLost);
        out.writeInt(enemyUnitCountLost);
        out.writeInt(enemyHealthLost);
        out.writeList(battleEvents);
    }

    public static final Parcelable.Creator<BattleSummaryParcelable> CREATOR
            = new Parcelable.Creator<BattleSummaryParcelable>() {
        public BattleSummaryParcelable createFromParcel(Parcel in) {
            return new BattleSummaryParcelable(in);
        }

        public BattleSummaryParcelable[] newArray(int size) {
            return new BattleSummaryParcelable[size];
        }
    };

    public boolean isDidFriendsWin() {
        return didFriendsWin;
    }

    public int getEnemyUnitCountLost() {
        return enemyUnitCountLost;
    }

    public int getEnemyHealthLost() {
        return enemyHealthLost;
    }

    public int getFriendlyUnitCountLost() {
        return friendlyUnitCountLost;
    }

    public int getFriendlyHealthLost() {
        return friendlyHealthLost;
    }

    public List<BattleEventParcelable> getBattleEvents() {
        return battleEvents;
    }
}
