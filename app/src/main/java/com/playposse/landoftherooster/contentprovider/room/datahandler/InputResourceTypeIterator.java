package com.playposse.landoftherooster.contentprovider.room.datahandler;

import android.support.annotation.NonNull;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.util.StringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An {@link Iterator} that takes a production rule, loads the ResourceTypes, and makes iterating
 * over them easy.
 */
public class InputResourceTypeIterator implements Iterator<ResourceType>, Iterable<ResourceType> {

    private final RoosterDao dao;
    private final ProductionRule productionRule;

    private Iterator<ResourceType> iterator;

    public InputResourceTypeIterator(RoosterDao dao, ProductionRule productionRule) {
        Objects.requireNonNull(dao);
        Objects.requireNonNull(productionRule);

        this.dao = dao;
        this.productionRule = productionRule;

        loadResourceTypes();
    }

    private void loadResourceTypes() {
        List<Long> resourceTypeIds = productionRule.getSplitInputResourceTypeIds();

        List<ResourceType> resourceTypes = dao.getResourceTypesById(resourceTypeIds);

        iterator = resourceTypes.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public ResourceType next() {
        return iterator.next();
    }

    @NonNull
    @Override
    public Iterator<ResourceType> iterator() {
        return this;
    }
}
