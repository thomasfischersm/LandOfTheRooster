package com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered;

import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;

import javax.annotation.Nullable;

/**
 * A {@link BusinessEvent} that is triggered after a battle building respawns.
 */
public class PostRespawnBattleBuildingEvent extends BusinessEvent{

    public PostRespawnBattleBuildingEvent(@Nullable Long buildingId) {
        super(buildingId);
    }
}
