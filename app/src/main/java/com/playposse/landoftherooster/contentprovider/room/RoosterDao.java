package com.playposse.landoftherooster.contentprovider.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;
import android.support.annotation.Nullable;

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

    @Query("select * from building_type where id > :lastBuildingTypeId order by id asc limit 1")
    BuildingType getNextBuildingType(int lastBuildingTypeId);

    @Query("select id from building_type")
    Cursor getCursorForBuildingTypeCount();

    @Query("select * from building order by id desc limit 1")
    @Nullable
    Building getLastBuilding();

    @Query("delete from resource_type")
    void deleteResourceTypes();

    @Query("delete from building_type")
    void deleteBuildingTypes();

    @Insert
    void insertBuilding(Building building);

    @Query("select *from building order by id asc")
    List<Building> getAllBuildings();
}
