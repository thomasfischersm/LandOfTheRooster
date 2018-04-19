package com.playposse.landoftherooster.dialog;

import android.content.Context;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;

/**
 * A data class that captures the data for an action. It contains the amount that the user
 * has with him/her and what is at the building.
 */
abstract class ActionData implements Runnable {

    public enum ActionType {
        DROP_OFF,
        PICKUP;
    }

    private final Context context;
    private final RoosterDao dao;

    ActionData(Context context, RoosterDao dao) {
        this.context = context;
        this.dao = dao;
    }

    @Override
    public void run() {
        performAction();
    }

    /**
     * Returns a string to describe the amount the user carries with him/her.
     */
    protected abstract String getUserString();

    /**
     * returns a string to describe the amount the building has.
     */
    protected abstract String getBuildingString();

    /**
     * Returns a string to label the possible user action.
     */
    protected abstract String getActionString();

    /**
     * Perform the action for a single unit.
     */
    protected abstract void performAction();

    /**
     * Checks if the action is available.
     */
    protected abstract boolean isAvailable();

    protected Context getContext() {
        return context;
    }

    protected RoosterDao getDao() {
        return dao;
    }
}
