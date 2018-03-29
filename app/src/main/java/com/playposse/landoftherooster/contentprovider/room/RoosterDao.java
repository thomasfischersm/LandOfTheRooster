package com.playposse.landoftherooster.contentprovider.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
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

    @Insert
    void insertUnitTypes(List<UnitType> rows);

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

    @Query("select * from building")
    List<Building> getAllBuildings();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.produced_resource_type_id as type_produced_resource_type_id, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    LiveData<List<BuildingWithType>> getAllBuildingsWithTypeAsLiveData();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.produced_resource_type_id as type_produced_resource_type_id, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    List<BuildingWithType> getAllBuildingsWithType();

    @Query("select * from resource_type where id=:resourceTypeId")
    ResourceType getResourceTypeById(int resourceTypeId);

    @Query("select * from resource where resource_type_id=:resourceTypeId")
    Resource getResourceByTypeId(int resourceTypeId);

    @Insert
    void insert(Resource resource);

    @Update
    void update(Resource resource);

    @Query("select sum(amount) from resource")
    int getResourceCount();

    @Query("select resource.id as id, resource.resource_type_id, resource.amount, resource_type.id as type_id, resource_type.name as type_name, resource_type.precursor_id as type_precursor_id from resource join resource_type on (resource.resource_type_id=resource_type.id) where amount > 0 order by resource_type.id asc")
    LiveData<List<ResourceWithType>> getAllResourcesWithType();
}
