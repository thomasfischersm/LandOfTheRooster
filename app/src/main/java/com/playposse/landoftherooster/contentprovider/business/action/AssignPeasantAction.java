package com.playposse.landoftherooster.contentprovider.business.action;

import com.playposse.landoftherooster.GameConfig;
import com.playposse.landoftherooster.analytics.Analytics;
import com.playposse.landoftherooster.contentprovider.business.BusinessAction;
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.PreconditionOutcome;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.ProductionRule;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.event.DaoEventRegistry;

import java.util.List;

/**
 * A {@link BusinessAction} that assigns a peasant to work at a building.
 */
public class AssignPeasantAction implements BusinessAction {

    private static final String LOG_TAG = AssignPeasantAction.class.getSimpleName();

    @Override
    public void perform(
            BusinessEvent event,
            PreconditionOutcome preconditionOutcome,
            BusinessDataCache dataCache) {

        // Transfer peasant from user to building.
        RoosterDao dao = dataCache.getDao();
        List<Unit> peasantsJoiningUser = dao.getUnitsJoiningUserByTypeId(GameConfig.PEASANT_ID);
        Unit peasant = peasantsJoiningUser.get(0);
        peasant.setLocatedAtBuildingId(dataCache.getBuildingId());
        DaoEventRegistry.get(dao)
                .update(peasant);
        dataCache.resetUnitMap();

        // Restart production if it has been sped up by the assignment.
        Building building = dataCache.getBuilding();
        List<ProductionRule> productionRules = dataCache.getProductionRules();
        if (building.getProductionStart() != null) {
            if (ProductionCycleUtil.hasFreeProductionRule(productionRules)) {
                CompleteFreeProductionEvent.schedule(dataCache);
            } else if ((productionRules != null) && (productionRules.size() > 0)) {
                CompleteProductionEvent.schedule(dataCache);
            }
        }

        // Fire post event.
        BusinessEngine.get()
                .triggerDelayedEvent(new PostAssignPeasantEvent(building.getId()));

        // Report event to analytics.
        Analytics.reportEvent(Analytics.AppEvent.ASSIGN_PEASANT);
    }
}
