package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.SkipQueryVerification;

import java.util.List;

/**
 * The Room DAO to read/write to the db.
 */
@Dao
public interface RoosterDao {

    @Query("select * from building_type")
    List<BuildingType> getAllBuildingTypes();
}
