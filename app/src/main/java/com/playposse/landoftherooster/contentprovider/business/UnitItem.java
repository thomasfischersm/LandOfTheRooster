package com.playposse.landoftherooster.contentprovider.business;

/**
 * Created by thoma on 5/2/2018.
 */
public class UnitItem extends Item {

    private final long unitTypeId;

    public UnitItem(long unitTypeId) {
        this.unitTypeId = unitTypeId;
    }

    public long getUnitTypeId() {
        return unitTypeId;
    }
}
