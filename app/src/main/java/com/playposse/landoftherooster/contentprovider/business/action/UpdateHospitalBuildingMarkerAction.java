package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.UnitInjuredEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A {@link BusinessAction} that updates the {@link MapMarker}s of healing buildings.
 */
public class UpdateHospitalBuildingMarkerAction extends BusinessAction {

    private static final String LOG_TAG = UpdateHospitalBuildingMarkerAction.class.getSimpleName();

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();
        Long buildingId = event.getBuildingId();

        if (event instanceof PostAdmitUnitToHospitalEvent) {
            PostAdmitUnitToHospitalEvent postAdmitEvent = (PostAdmitUnitToHospitalEvent) event;
            updateHospitalBuilding(dataCache);

            if (postAdmitEvent.isLastInjuredUnitAdmitted()) {
                updateOtherHospitalBuildingsAfterAdmission(dao, buildingId);
            }
        } else if (event instanceof UnitInjuredEvent) {
            if (dataCache.hasInjuredUnitJoiningUser()) {
                updateOtherHospitalBuildingsAfterInjury(dao);
            } else {
                // Perhaps an injured unit was killed off. Therefore, there are no more injured
                // units, who could be dropped off at a hospital.
                updateOtherHospitalBuildingsAfterAdmission(dao, null);
            }
        } else if (event instanceof PostCompleteHealingEvent) {
            updateHospitalBuilding(dataCache);
        } else if (event instanceof PostPickUpUnitFromHospitalEvent) {
            updateHospitalBuilding(dataCache);

            // Update
        }
    }

    private void updateHospitalBuilding(BusinessDataCache dataCache) {
        RoosterDao dao = dataCache.getDao();

        int healingUnitCount = dataCache.getHealingUnitCount();
        int recoveredUnitCount = dataCache.getRecoveredUnitCount();
        boolean hasInjuredUnitJoiningUser = dataCache.hasInjuredUnitJoiningUser();
        boolean isReady = (recoveredUnitCount > 0) || hasInjuredUnitJoiningUser;

        MapMarker mapMarker = dataCache.getMapMarker();

        if (mapMarker == null) {
            throw new IllegalStateException("The building is missing a map marker: "
                    + dataCache.getBuildingId());
        }

        if ((mapMarker.isReady() != isReady)
                || (mapMarker.getPendingProductionCount() != healingUnitCount)
                || (mapMarker.getCompletedProductionCount() != recoveredUnitCount)) {
            mapMarker.setReady(isReady);
            mapMarker.setPendingProductionCount(healingUnitCount);
            mapMarker.setCompletedProductionCount(recoveredUnitCount);
            DaoEventRegistry.get(dao)
                    .update(mapMarker);
        }
    }

    /**
     * Updates {@link MapMarker}s to indicate not ready if waiting to receive an injured unit was
     * their only possible action.
     */
    private void updateOtherHospitalBuildingsAfterAdmission(
            RoosterDao dao,
            @Nullable Long eventBuildingId) {

        List<MapMarker> mapMarkers = dao.getMapMarkersOfHealingBuildings();

        if (mapMarkers != null) {
            for (MapMarker mapMarker : mapMarkers) {
                Long buildingId = mapMarker.getBuildingId();
                if ((buildingId != null)
                        && (eventBuildingId != null)
                        && (buildingId.equals(eventBuildingId))) {
                    continue;
                }

                boolean noCompletedProductionCount =
                        ((mapMarker.getCompletedProductionCount() == null)
                                || (mapMarker.getCompletedProductionCount() == 0));
                // No injured unit joining user is implied in call.
                if (noCompletedProductionCount) {
                    mapMarker.setReady(false);
                    DaoEventRegistry.get(dao)
                            .update(mapMarker);
                }
            }
        }
    }

    private void updateOtherHospitalBuildingsAfterInjury(RoosterDao dao) {
        List<MapMarker> mapMarkers = dao.getMapMarkersOfHealingBuildings();

        if (mapMarkers != null) {
            for (MapMarker mapMarker : mapMarkers) {
                if (!mapMarker.isReady()) {
                    mapMarker.setReady(true);
                    DaoEventRegistry.get(dao)
                            .update(mapMarker);
                }
            }
        }
    }
}
