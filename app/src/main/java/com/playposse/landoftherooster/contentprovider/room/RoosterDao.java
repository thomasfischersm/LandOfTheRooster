package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

/**
 * The Room DAO to read/write to the db.
 */
@Dao
public interface RoosterDao {

    @Insert
    void insertBuildingTypes(List<BuildingType> buildingTypes);

    @Insert
    void insertResourceTypes(List<ResourceType> resourceTypes);

    @Query("select * from building_type")
    List<BuildingType> getAllBuildingTypes();

    @Query("select id from building_type")
    Cursor getCursorForBuildingTypeCount();
}
