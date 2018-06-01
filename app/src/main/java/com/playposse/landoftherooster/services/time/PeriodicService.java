package com.playposse.landoftherooster.services.time;

import android.content.Context;

import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A base class for services that run on a periodic scheduleWithDefaultDelay.
 */
public abstract class PeriodicService {

    private final Context context;
    private final RoosterDao dao;

    private ScheduledExecutorService scheduledExecutorService;

    public PeriodicService(Context context) {
        this.context = context;

        dao = RoosterDatabase.getInstance(context).getDao();
    }

    public void start() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        PeriodicService.this.run();
                    }
                },
                0,
                getPeriodMs(),
                TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduledExecutorService.shutdownNow();
    }

    protected abstract long getPeriodMs();

    protected abstract void run();

    protected Context getContext() {
        return context;
    }

    protected RoosterDao getDao() {
        return dao;
    }
}
