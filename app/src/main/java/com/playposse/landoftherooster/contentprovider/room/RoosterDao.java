package com.playposse.landoftherooster.contentprovider.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

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

    @Insert
    void insertProductionRules(List<ProductionRule> productionRules);

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

    @Delete
    void deleteUnit(Unit unit);

    @Query("delete from unit")
    void deleteUnits();

    @Insert
    void insertBuilding(Building building);

    @Query("select * from building")
    List<Building> getAllBuildings();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    LiveData<List<BuildingWithType>> getAllBuildingsWithTypeAsLiveData();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    List<BuildingWithType> getAllBuildingsWithType();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id from building join building_type on (building.building_type_id = building_type.id) where building.id=:buildingId")
    BuildingWithType getBuildingWithTypeByBuildingId(int buildingId);

    @Query("select * from resource_type where id=:resourceTypeId")
    ResourceType getResourceTypeById(int resourceTypeId);

    @Query("select * from unit_type where id=:unitTypeId")
    UnitType getUnitTypeById(int unitTypeId);

    @Query("select * from resource where resource_type_id=:resourceTypeId")
    Resource getResourceByTypeId(int resourceTypeId);

    @Query("select * from unit where unit_type_id=:unitTypeId")
    List<Unit> getUnitsByTypeId(int unitTypeId);

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as location_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc")
    List<UnitWithType> getUnitsWithTypeJoiningUser();

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as location_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc")
    LiveData<List<UnitWithType>> getUnitsWithTypeJoiningUserAsLiveData();

    @Query("select * from production_rule where production_rule.building_id=:buildingTypeId")
    List<ProductionRule> getProductionRulesByBuildingTypeId(int buildingTypeId);

    @Insert
    void insert(Resource resource);

    @Insert
    void insert(Unit unit);

    @Update
    void update(Resource resource);

    @Update
    void update(Unit unit);

    @Update
    void update(Building building);

    @Query("select sum(amount) from resource")
    int getResourceCount();

    @Query("select sum(unit_type.carrying_capacity) from unit join unit_type on (unit.unit_type_id = unit_type.id)")
    int getCarryingCapacity();

    @Query("select resource.id as id, resource.resource_type_id, resource.amount, resource_type.id as type_id, resource_type.name as type_name from resource join resource_type on (resource.resource_type_id=resource_type.id) where amount > 0 order by resource_type.id asc")
    LiveData<List<ResourceWithType>> getAllResourcesWithType();
}
