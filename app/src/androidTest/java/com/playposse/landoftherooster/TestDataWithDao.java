package com.playposse.landoftherooster;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import org.junit.Before;

/**
 * A base test class that provides methods for testing data and a {@link RoosterDao} accesible as a
 * field.
 */
public class TestDataWithDao extends TestData {

    protected RoosterDao dao;
    protected Context targetContext;

    @Before
    public void setUp() throws InterruptedException {
        targetContext = InstrumentationRegistry.getTargetContext();
        dao = RoosterDatabase.getInstance(targetContext).getDao();
    }
}
