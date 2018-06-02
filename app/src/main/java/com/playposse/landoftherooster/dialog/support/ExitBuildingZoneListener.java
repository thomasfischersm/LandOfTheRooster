package com.playposse.landoftherooster.dialog.support;

import android.app.DialogFragment;

import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessEventListener;
import com.playposse.landoftherooster.contentprovider.business.event.locationTriggered.BuildingZoneExitedEvent;

/**
 * A {@link BusinessEventListener} that listens to the {@link BuildingZoneExitedEvent} to close
 * the specified dialog.
 */
public class ExitBuildingZoneListener implements BusinessEventListener {

    private final DialogFragment dialogFragment;

    public ExitBuildingZoneListener(DialogFragment dialogFragment) {
        this.dialogFragment = dialogFragment;
    }

    @Override
    public void onEvent(BusinessEvent event, BusinessDataCache cache) {
        dialogFragment.dismiss();
    }
}
