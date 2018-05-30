package com.playposse.landoftherooster.contentprovider.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Nullable;

import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitCountByType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;

import java.util.List;

/**
 * The Room DAO to read/write to the db.
 */
@Dao
public interface RoosterDao {

    // Building types
    @Insert
    void insertBuildingTypes(List<BuildingType> buildingTypes);

    @Query("select * from building_type where id = :buildingTypeId")
    BuildingType getBuildingType(long buildingTypeId);

    @Query("select * from building_type")
    List<BuildingType> getAllBuildingTypes();

    @Query("select * from building_type where id > :lastBuildingTypeId order by id asc limit 1")
    BuildingType getNextBuildingType(long lastBuildingTypeId);

    @Query("select count(id) from building_type")
    int getBuildingTypeCount();

    @Query("delete from building_type")
    void deleteBuildingTypes();


    // Buildings
    @Insert
    long insert(Building building);

    @Update
    void update(Building building);

    @Query("select * from building where id = :buildingId")
    Building getBuildingById(long buildingId);

    @Query("select * from building order by id desc limit 1")
    @Nullable
    Building getLastBuilding();

    @Query("select * from building")
    List<Building> getAllBuildings();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    LiveData<List<BuildingWithType>> getAllBuildingsWithTypeAsLiveData();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) order by building.id asc")
    List<BuildingWithType> getAllBuildingsWithType();

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) where building.id=:buildingId")
    BuildingWithType getBuildingWithTypeByBuildingId(long buildingId);

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) where building_type.id in (:buildingTypeIds)")
    List<BuildingWithType> getBuildingWithTypeByBuildingTypeIds(List<Long> buildingTypeIds);

    @Query("select building.id as id, building.building_type_id, building.latitude, building.longitude, building.last_conquest, building.production_start, building.healing_started, building_type.id as type_id, building_type.name as type_name, building_type.icon as type_icon, building_type.min_distance_meters as type_min_distance_meters, building_type.max_distance_meters as type_max_distance_meters, building_type.enemy_unit_count as type_enemy_unit_count, building_type.enemy_unit_type_id as type_enemy_unit_type_id, building_type.conquest_prize_resource_type_id as type_conquest_prize_resource_type_id, building_type.heals_units as type_heals_units from building join building_type on (building.building_type_id = building_type.id) where building_type.heals_units = 1")
    List<BuildingWithType> getHealingBuildingsWithType();

    @Query("delete from building")
    void deleteBuildings();


    // Production rules
    @Insert
    void insertProductionRules(List<ProductionRule> productionRules);

    @Query("select * from production_rule where production_rule.building_type_id=:buildingTypeId")
    List<ProductionRule> getProductionRulesByBuildingTypeId(long buildingTypeId);

    @Query("select * from production_rule")
    List<ProductionRule> getAllProductionRules();


    // Resource types
    @Insert
    void insertResourceTypes(List<ResourceType> resourceTypes);

    @Query("select * from resource_type where id=:resourceTypeId")
    ResourceType getResourceTypeById(long resourceTypeId);

    @Query("select * from resource_type where id in (:resourceTypeIds)")
    List<ResourceType> getResourceTypesById(List<Long> resourceTypeIds);

    @Query("delete from resource_type")
    void deleteResourceTypes();


    // Resources
    @Insert
    long insert(Resource resource);

    @Update
    void update(Resource resource);

    @Query("select * from resource where resource_type_id=:resourceTypeId and located_at_building_id is null")
    Resource getResourceJoiningUserByTypeId(long resourceTypeId);

    @Query("select resource.id as id, resource.resource_type_id, resource.amount, resource.located_at_building_id as located_at_building_id, resource_type.id as type_id, resource_type.name as type_name from resource join resource_type on (resource.resource_type_id=resource_type.id) where amount > 0 and resource_type.id = :resourceTypeId and resource.located_at_building_id is null order by resource_type.id asc")
    ResourceWithType getResourceWithTypeJoiningUser(long resourceTypeId);

    @Query("select resource.id as id, resource.resource_type_id, resource.amount, resource.located_at_building_id as located_at_building_id, resource_type.id as type_id, resource_type.name as type_name from resource join resource_type on (resource.resource_type_id=resource_type.id) where amount > 0 and resource_type.id = :resourceTypeId and resource.located_at_building_id = :buildingId order by resource_type.id asc")
    ResourceWithType getResourceWithType(long resourceTypeId, Long buildingId);

    @Query("select resource.id as id, resource.resource_type_id, resource.amount, resource.located_at_building_id as located_at_building_id, resource_type.id as type_id, resource_type.name as type_name from resource join resource_type on (resource.resource_type_id=resource_type.id) where amount > 0 and located_at_building_id is null order by resource_type.id asc")
    LiveData<List<ResourceWithType>> getAllResourcesWithTypeJoiningUser();

    @Query("select * from resource where located_at_building_id=:buildingId")
    List<Resource> getResourcesByBuildingId(long buildingId);

    @Query("select * from resource")
    LiveData<List<Resource>> getAllResourcesAsLiveData();

    @Query("select * from resource where located_at_building_id is null")
    List<Resource> getAllResourcesJoiningUser();

    @Query("select sum(amount) from resource where located_at_building_id is null")
    int getResourceCountJoiningUser();

    @Delete
    void delete(Resource resource);

    @Query("delete from resource")
    void deleteResources();


    // Unit types
    @Insert
    void insertUnitTypes(List<UnitType> rows);

    @Query("select * from unit_type where id=:unitTypeId")
    UnitType getUnitTypeById(long unitTypeId);

    @Query("select * from unit_type where id in (:unitTypeIds)")
    List<UnitType> getUnitTypesById(List<Long> unitTypeIds);

    @Query("select * from unit_type")
    List<UnitType> getAllUnitTypes();


    // Units
    @Insert
    long insert(Unit unit);

    @Update
    void update(Unit unit);

    @Query("select * from unit where id = :unitId")
    Unit getUnitById(long unitId);

    @Query("select * from unit where unit_type_id=:unitTypeId and located_at_building_id is null")
    List<Unit> getUnitsJoiningUserByTypeId(long unitTypeId);

    @Query("select * from unit where unit_type_id=:unitTypeId and located_at_building_id=:buildingId")
    List<Unit> getUnits(long unitTypeId, long buildingId);

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc")
    List<UnitWithType> getUnitsWithTypeJoiningUser();

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null and unit.id = :unitId")
    UnitWithType getUnitWithTypeJoiningUser(long unitId);

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null and unit.health < unit_type.health order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc")
    List<UnitWithType> getWoundedUnitsWithTypeJoiningUser();

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id = :buildingId order by unit.health desc, unit_type.attack desc, unit_type.id desc, unit.id asc")
    List<UnitWithType> getUnitsWithTypeByBuildingId(long buildingId);

    @Query("select unit.id as id, unit.unit_type_id as unit_type_id, unit.health as health, unit.located_at_building_id as located_at_building_id, unit_type.id as type_id, unit_type.name as type_name, unit_type.carrying_capacity as type_carrying_capacity, unit_type.attack as type_attack, unit_type.defense as type_defense, unit_type.damage as type_damage, unit_type.armor as type_armor, unit_type.health as type_health from unit join unit_type on (unit.unit_type_id = unit_type.id) where located_at_building_id is null order by unit_type.attack desc, unit.unit_type_id asc, unit.health desc, unit.id asc")
    LiveData<List<UnitWithType>> getUnitsWithTypeJoiningUserAsLiveData();

    @Query("select sum(unit_type.carrying_capacity) from unit join unit_type on (unit.unit_type_id = unit_type.id) where unit.located_at_building_id is null")
    int getCarryingCapacity();

    @Query("select count(*) from unit where unit_type_id=:unitTypeId and located_at_building_id is null")
    int getUnitCountJoiningUser(long unitTypeId);

    @Query("select count(*) from unit where unit_type_id=:unitTypeId and located_at_building_id=:buildingId")
    int getUnitCount(long unitTypeId, long buildingId);

    @Query("select unit_type_id, count(id) as count from unit where located_at_building_id = :buildingId group by unit_type_id")
    List<UnitCountByType> getUnitCountByBuilding(long buildingId);

    @Query("select unit_type_id, count(id) as count from unit where located_at_building_id is null group by unit_type_id")
    List<UnitCountByType> getUnitCountsJoiningUser();

    @Query("Select * from unit")
    LiveData<List<Unit>> getAllUnitsAsLiveData();

    @Delete
    void delete(Unit unit);

    @Query("delete from unit")
    void deleteUnits();


    // Map markers
    @Insert
    long insert(MapMarker mapMarker);

    @Update
    void update(MapMarker mapMarker);

    @Query("select * from map_marker where building_id in (:buildingIds)")
    List<MapMarker> getMapMarkerByBuildingIds(List<Long> buildingIds);

    @Query("select * from map_marker where building_id =:buildingId")
    MapMarker getMapMarkerByBuildingId(Long buildingId);

    @Query("select map_marker.* from map_marker join building_type on (building_type.id = map_marker.building_type_id) where building_type.heals_units = 1")
    List<MapMarker> getMapMarkersOfHealingBuildings();

    @Query("delete from map_marker")
    void deleteMapMarkers();
}
