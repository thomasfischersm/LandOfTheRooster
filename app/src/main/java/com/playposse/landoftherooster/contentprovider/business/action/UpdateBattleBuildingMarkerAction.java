package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.MapMarker;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

/**
 * A {@link BusinessAction} that handles updating a {@link MapMarker} for a battle building.
 */
public class UpdateBattleBuildingMarkerAction extends BusinessAction {

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        RoosterDao dao = dataCache.getDao();

        // Update MapMarker.
        Building building = dataCache.getBuilding();
        MapMarker mapMarker = dataCache.getMapMarker();
        boolean isReadyForBattle = building.getLastConquest() == null;
        mapMarker.setReady(isReadyForBattle);
        DaoEventRegistry.get(dao)
                .update(mapMarker);
    }
}
