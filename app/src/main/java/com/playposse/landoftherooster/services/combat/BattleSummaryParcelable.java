package com.playposse.landoftherooster.services.combat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data class that describes the battle outcome.
 */
public class BattleSummaryParcelable implements Parcelable {

    private final boolean didFriendsWin;

    public BattleSummaryParcelable(boolean didFriendsWin) {
        this.didFriendsWin = didFriendsWin;
    }

    private BattleSummaryParcelable(Parcel in) {
        didFriendsWin = (in.readByte() != 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (didFriendsWin ? 1 : 0));
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
}
