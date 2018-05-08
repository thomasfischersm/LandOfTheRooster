package com.playposse.landoftherooster.contentprovider.business;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.playposse.landoftherooster.contentprovider.business.action.FreeProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.ProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.StartFreeItemProductionAction;
import com.playposse.landoftherooster.contentprovider.business.action.StartItemProductionAction;
import com.playposse.landoftherooster.contentprovider.business.event.BuildingCreatedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.FreeItemProductionEndedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.ItemProductionEndedEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserDropsOffItemEvent;
import com.playposse.landoftherooster.contentprovider.business.event.UserPicksUpItemEvent;
import com.playposse.landoftherooster.contentprovider.business.precondition.FreeProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.ProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.StartFreeItemProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.business.precondition.StartItemProductionPrecondition;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.util.CancelableRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The central code that handles business events and wires different actions together.
 */
public class BusinessEngine {

    private static final String LOG_TAG = BusinessEngine.class.getSimpleName();

    private final ListMultimap<Class<? extends BusinessEvent>, ActionContainer> registry =
            ArrayListMultimap.create();

    private static BusinessEngine instance;

    private RoosterDao dao;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Map for {@link BusinessEvent} to its {@link Runnable}. The purpose is to be able to
     * recognize if a certain type of event for a certain building is already scheduled. Only the
     * sooner event should survive to avoid executing unnecessary events and draining the battery.
     */
    private Map<BusinessEvent, CancelableRunnable> eventToRunnableMap = new HashMap<>();

    private BusinessEngine() {
        // Initiate all the actions.
        registerAction(
                UserDropsOffItemEvent.class,
                new StartItemProductionPrecondition(),
                new StartItemProductionAction());

        registerAction(
                ItemProductionEndedEvent.class,
                new ProductionPrecondition(),
                new ProductionAction());

        // TODO schedule action for after item has been produced to check the next production start.

        registerAction(
                UserPicksUpItemEvent.class,
                new StartFreeItemProductionPrecondition(),
                new StartFreeItemProductionAction());

        registerAction(
                BuildingCreatedEvent.class,
                new FreeProductionPrecondition(),
                new FreeProductionAction());

        registerAction(
                FreeItemProductionEndedEvent.class,
                new FreeProductionPrecondition(),
                new FreeProductionAction());
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
        scheduledExecutorService.shutdownNow();
        scheduledExecutorService = null;
    }

    public void triggerEvent(BusinessEvent event) {
        Log.i(LOG_TAG, "triggerEvent: Triggered event: [" + event.getClass().getSimpleName()
                + "]");
        BusinessDataCache dataCache = new BusinessDataCache(dao, event.getBuildingId());

        for (ActionContainer actionContainer : registry.get(event.getClass())) {
            // Try precondition.
            PreconditionOutcome preconditionOutcome =
                    actionContainer.getPrecondition().evaluate(event, dataCache);
            if (!preconditionOutcome.getSuccess()) {
                Log.i(LOG_TAG, "triggerEvent: Precondition wasn't satisfied: "
                        + actionContainer.getPrecondition().getClass().getSimpleName());
                continue;
            }

            // Execute action.
            Log.i(LOG_TAG, "triggerEvent: Start action ["
                    + actionContainer.getAction().getClass().getSimpleName() + "]");
            actionContainer.getAction().perform(event, preconditionOutcome, dataCache);
            Log.i(LOG_TAG, "triggerEvent: Finished action ["
                    + actionContainer.getAction().getClass().getSimpleName() + "]");
        }
        Log.i(LOG_TAG, "triggerEvent: Completed event: [" + event.getClass().getSimpleName()
                + "]");
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

    private void registerAction(
            Class<? extends BusinessEvent> eventClass,
            BusinessPrecondition precondition,
            BusinessAction action) {

        registry.put(eventClass, new ActionContainer(eventClass, precondition, action));
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