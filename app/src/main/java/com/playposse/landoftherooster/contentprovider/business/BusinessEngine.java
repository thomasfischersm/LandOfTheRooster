package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.playposse.landoftherooster.analytics.Analytics;
import com.playposse.landoftherooster.contentprovider.business.action.AdmitUnitToHospitalAction;
import com.playposse.landoftherooster.contentprovider.business.action.AssignPeasantAction;
import com.playposse.landoftherooster.contentprovider.business.action.CompleteHealingAction;
import com.playposse.landoftherooster.contentprovider.business.action.CreateBuildingAction;
import com.playposse.landoftherooster.contentprovider.business.action.DropOffItemAction;
import com.playposse.landoftherooster.contentprovider.business.action.ExecuteBattleAction;
import com.playposse.landoftherooster.contentprovider.business.action.FreeProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.InitiateHealingAction;
import com.playposse.landoftherooster.contentprovider.business.action.PickUpItemAction;
import com.playposse.landoftherooster.contentprovider.business.action.PickUpUnitFromHospitalAction;
import com.playposse.landoftherooster.contentprovider.business.action.ProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.RespawnBattleBuildingAction;
import com.playposse.landoftherooster.contentprovider.business.action.InitiateFreeProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.InitiateProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.UpdateHospitalBuildingMarkerAction;
import com.playposse.landoftherooster.contentprovider.business.action.UpdateProductionBuildingMarkerAction;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostAdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteFreeProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostCompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostDropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostPickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.UnitInjuredEvent;
import com.playposse.landoftherooster.contentprovider.business.event.mixedTriggered.InitiateHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.other.LocationUpdateEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteFreeItemProduction;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteHealingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.CompleteProductionEvent;
import com.playposse.landoftherooster.contentprovider.business.event.timeTriggered.RespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AdmitUnitToHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.AssignPeasantEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.DropOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.InitiateBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.PickUpUnitFromHospitalEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.AdmitUnitToHospitalPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.AssignPeasantPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.CompleteHealingPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.CreateBuildingPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.DropOffItemPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.ExecuteBattlePrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.FreeProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.InitiateHealingPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpItemPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.PickUpUnitFromHospitalPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.ProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.RespawnBattleBuildingPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.InitiateFreeProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.InitiateProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.UpdateHospitalBuildingMarkerPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.UpdateProductionBuildingMarkerPrecondition;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.CancelableRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The central code that handles business events and wires different actions together.
 */
public class BusinessEngine {

    private static final String LOG_TAG = BusinessEngine.class.getSimpleName();
    private static BusinessEngine instance;

    private final ListMultimap<Class<? extends BusinessEvent>, ActionContainer> registry =
            ArrayListMultimap.create();
    private final List<BusinessEvent> eventQueue = new ArrayList<>();

    private RoosterDao dao;
    private ScheduledExecutorService scheduledExecutorService;
    private int executedEventCounter = 0;

    /**
     * Map for {@link BusinessEvent} to its {@link Runnable}. The purpose is to be able to
     * recognize if a certain type of event for a certain building is already scheduled. Only the
     * sooner event should survive to avoid executing unnecessary events and draining the battery.
     */
    private Map<BusinessEvent, CancelableRunnable> eventToRunnableMap = new HashMap<>();

    private BusinessEngine() {
        // Initiate all the actions.

        // item production for cost
        registerAction(
                DropOffItemEvent.class,
                new DropOffItemPrecondition(),
                new DropOffItemAction());

        registerAction(
                PostDropOffItemEvent.class,
                new InitiateProductionPrecondition(),
                new InitiateProductionAction());

        registerAction(
                CompleteProductionEvent.class,
                new ProductionPrecondition(),
                new ProductionAction());

        registerAction(
                PostCompleteProductionEvent.class,
                new InitiateProductionPrecondition(),
                new InitiateProductionAction());

        // TODO schedule action for after item has been produced to check the next production start.

        // free item production
        registerAction(
                PickUpItemEvent.class,
                new PickUpItemPrecondition(),
                new PickUpItemAction());

        registerAction(
                PostPickUpItemEvent.class,
                new InitiateFreeProductionPrecondition(),
                new InitiateFreeProductionAction());

        registerAction(
                BuildingCreatedEvent.class,
                new FreeProductionPrecondition(),
                new FreeProductionAction());

        registerAction(
                CompleteFreeItemProduction.class,
                new FreeProductionPrecondition(),
                new FreeProductionAction());

        // building creation
        registerAction(
                LocationUpdateEvent.class,
                new CreateBuildingPrecondition(),
                new CreateBuildingAction());

        // Assign peasant user action
        registerAction(
                AssignPeasantEvent.class,
                new AssignPeasantPrecondition(),
                new AssignPeasantAction());

        // building battle
        registerAction(
                InitiateBattleEvent.class,
                new ExecuteBattlePrecondition(),
                new ExecuteBattleAction());

        registerAction(
                RespawnBattleBuildingEvent.class,
                new RespawnBattleBuildingPrecondition(),
                new RespawnBattleBuildingAction());

        // hospital events
        registerAction(
                AdmitUnitToHospitalEvent.class,
                new AdmitUnitToHospitalPrecondition(),
                new AdmitUnitToHospitalAction());

        registerAction(
                PostAdmitUnitToHospitalEvent.class,
                new UpdateHospitalBuildingMarkerPrecondition(),
                new UpdateHospitalBuildingMarkerAction());

        registerAction(
                UnitInjuredEvent.class,
                new UpdateHospitalBuildingMarkerPrecondition(),
                new UpdateHospitalBuildingMarkerAction());

        registerAction(
                InitiateHealingEvent.class,
                new InitiateHealingPrecondition(),
                new InitiateHealingAction());

        registerAction(
                CompleteHealingEvent.class,
                new CompleteHealingPrecondition(),
                new CompleteHealingAction());

        registerAction(
                PostCompleteHealingEvent.class,
                new UpdateHospitalBuildingMarkerPrecondition(),
                new UpdateHospitalBuildingMarkerAction());

        registerAction(
                PickUpUnitFromHospitalEvent.class,
                new PickUpUnitFromHospitalPrecondition(),
                new PickUpUnitFromHospitalAction());

        registerAction(
                PostPickUpUnitFromHospitalEvent.class,
                new UpdateHospitalBuildingMarkerPrecondition(),
                new UpdateHospitalBuildingMarkerAction());

        registerAction(
                PostPickUpUnitFromHospitalEvent.class,
                new UpdateProductionBuildingMarkerPrecondition(),
                new UpdateProductionBuildingMarkerAction());

        // MOST COME LAST!
        // Update building markers.
        registerAction(
                PostPickUpItemEvent.class,
                new UpdateProductionBuildingMarkerPrecondition(),
                new UpdateProductionBuildingMarkerAction());

        registerAction(
                PostDropOffItemEvent.class,
                new UpdateProductionBuildingMarkerPrecondition(),
                new UpdateProductionBuildingMarkerAction());

        registerAction(
                PostCompleteProductionEvent.class,
                new UpdateProductionBuildingMarkerPrecondition(),
                new UpdateProductionBuildingMarkerAction());

        registerAction(
                PostCompleteFreeProductionEvent.class,
                new UpdateProductionBuildingMarkerPrecondition(),
                new UpdateProductionBuildingMarkerAction());
    }

    public static BusinessEngine get() {
        if (instance == null) {
            instance = new BusinessEngine();
        }
        return instance;
    }

    public void start(Context context) {
        dao = RoosterDatabase.getInstance(context).getDao();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void stop() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
            eventQueue.clear();
            executedEventCounter = 0;
        }
    }

    /**
     * Triggers a {@link BusinessEvent} to be executed immediately.
     */
    public void triggerEvent(BusinessEvent event) {
        executeEvent(event);

        while (eventQueue.size() > 0) {
            BusinessEvent currentEvent = eventQueue.remove(0);
            executeEvent(currentEvent);
        }
    }

    /**
     * Triggers an event to be executed via a queue. If another event is currently executing, the
     * specified event will be executed right after that.
     */
    public void triggerDelayedEvent(BusinessEvent event) {
        eventQueue.add(event);
    }

    private void executeEvent(BusinessEvent event) {
        Log.i(LOG_TAG, "triggerEvent: Triggered event: [" + event.getClass().getSimpleName()
                + "]");

        // Trace method duration in analytics.
        long eventStart = System.currentTimeMillis();
        Trace trace = FirebasePerformance.getInstance().newTrace("BusinessEvent.executeTrace");
        trace.putAttribute(Analytics.EVENT_NAME_ATTRIBUTE, event.getClass().getSimpleName());
        trace.start();

        BusinessDataCache dataCache = new BusinessDataCache(dao, event.getBuildingId());

        for (ActionContainer actionContainer : registry.get(event.getClass())) {
            // Try precondition.
            long preconditionStart = System.currentTimeMillis();
            PreconditionOutcome preconditionOutcome =
                    actionContainer.getPrecondition().evaluate(event, dataCache);
            long preconditionEnd = System.currentTimeMillis();
            Log.i(LOG_TAG, "executeEvent: Evaluated precondition ["
                    + actionContainer.getPrecondition().getClass().getSimpleName() + "] in "
                    + (preconditionEnd - preconditionStart) + " ms");

            if (!preconditionOutcome.getSuccess()) {
                trace.incrementMetric(Analytics.PRECONDITION_FAILURE_ATTRIBUTE, 1);
                Log.i(LOG_TAG, "triggerEvent: Precondition wasn't satisfied: "
                        + actionContainer.getPrecondition().getClass().getSimpleName());
                continue;
            }

            // Execute action.
            trace.incrementMetric(Analytics.PRECONDITION_SUCCESS_ATTRIBUTE, 1);

            Log.i(LOG_TAG, "triggerEvent: Start action ["
                    + actionContainer.getAction().getClass().getSimpleName() + "]");
            long actionStart = System.currentTimeMillis();
            actionContainer.getAction().perform(event, preconditionOutcome, dataCache);
            executedEventCounter++;
            long actionEnd = System.currentTimeMillis();
            Log.i(LOG_TAG, "triggerEvent: Finished action ["
                    + actionContainer.getAction().getClass().getSimpleName() + "] in "
                    + (actionEnd - actionStart) + " ms");
        }

        // End trace.
        trace.stop();
        Analytics.logBusinessEvent(event, eventStart);

        long eventEnd = System.currentTimeMillis();
        Log.i(LOG_TAG, "triggerEvent: Completed event: [" + event.getClass().getSimpleName()
                + "] in " + (eventEnd - eventStart) + "ms");
    }

    public void scheduleEvent(long delayMs, final BusinessEvent event) {
        Log.i(LOG_TAG, "scheduleEvent: Scheduled event [" + event.getClass().getSimpleName()
                + "] for in " + delayMs + "ms.");

        // Cancel any previously scheduled event.
        if (eventToRunnableMap.containsKey(event)) {
            CancelableRunnable earlierRunnable = eventToRunnableMap.get(event);
            earlierRunnable.cancel();
            Log.i(LOG_TAG, "scheduleEvent: Canceld previously scheduled event.");
        }

        CancelableRunnable runnable = new CancelableRunnable() {
            @Override
            protected void maybeRun() {
                triggerEvent(event);
            }
        };

        scheduledExecutorService.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
        eventToRunnableMap.put(event, runnable);
    }

    @VisibleForTesting
    void registerAction(
            Class<? extends BusinessEvent> eventClass,
            BusinessPrecondition precondition,
            BusinessAction action) {

        registry.put(eventClass, new ActionContainer(eventClass, precondition, action));
    }

    @VisibleForTesting
    public int getExecutedEventCounter() {
        return executedEventCounter;
    }

    /**
     * A data structure to keep all action related objects together.
     */
    private static class ActionContainer {

        private final Class<? extends BusinessEvent> eventClass;
        private final BusinessPrecondition precondition;
        private final BusinessAction action;

        private ActionContainer(
                Class<? extends BusinessEvent> eventClass,
                BusinessPrecondition precondition,
                BusinessAction action) {

            this.eventClass = eventClass;
            this.precondition = precondition;
            this.action = action;
        }

        private Class<? extends BusinessEvent> getEventClass() {
            return eventClass;
        }

        private BusinessPrecondition getPrecondition() {
            return precondition;
        }

        private BusinessAction getAction() {
            return action;
        }
    }
}
