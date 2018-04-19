package com.playposse.landoftherooster.contentprovider.room.datahandler;

import android.support.annotation.NonNull;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An {@link Iterator} that takes a production rule, loads the {@link UnitType}s, and makes
 * iterating over them easy.
 */
public class InputUnitTypeIterator implements Iterator<UnitType>, Iterable<UnitType> {

    private final RoosterDao dao;
    private final ProductionRule productionRule;

    private Iterator<UnitType> iterator;

    public InputUnitTypeIterator(RoosterDao dao, ProductionRule productionRule) {
        Objects.requireNonNull(dao);
        Objects.requireNonNull(productionRule);

        this.dao = dao;
        this.productionRule = productionRule;

        loadUnitTypes();
    }

    private void loadUnitTypes() {
        List<Long> unitTypeIds =
                StringUtil.splitToLongList(productionRule.getInputUnitTypeIds());

        List<UnitType> unitTypes = dao.getUnitTypesById(unitTypeIds);

        iterator = unitTypes.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public UnitType next() {
        return iterator.next();
    }

    @NonNull
    @Override
    public Iterator<UnitType> iterator() {
        return this;
    }
}
