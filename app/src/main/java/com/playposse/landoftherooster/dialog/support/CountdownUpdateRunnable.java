package com.playposse.landoftherooster.dialog.support;

import android.widget.TextView;

import com.playposse.landoftherooster.R;

import javax.annotation.Nullable;

/**
 * A {@link Runnable} that updates the countdown clock.
 */
public class CountdownUpdateRunnable implements Runnable {

    private static final int SECOND_IN_MS = 1_000;

    private final TextView countdownTextView;
    @Nullable private final Runnable countdownCompleteRunnable;

    private long remainingMs;

    public CountdownUpdateRunnable(
            TextView countdownTextView,
            long remainingMs,
            @Nullable Runnable countdownCompleteRunnable) {

        this.countdownTextView = countdownTextView;
        this.remainingMs = remainingMs;
        this.countdownCompleteRunnable = countdownCompleteRunnable;
    }

    @Override
    public void run() {
        // Stop countdown at 0.
        if (remainingMs == 0) {
            return;
        }

        // Increment time.
        remainingMs -= SECOND_IN_MS;
        remainingMs = Math.max(0, remainingMs);

        // Calculate time.
        long seconds = remainingMs / 1_000 % 60;
        long minutes = remainingMs / (60 * 1_000) % 60;
        long hours = remainingMs / (60 * 60 * 1_000);

        // Update TextView
        final String str = countdownTextView.getContext().getString(
                R.string.building_needs_to_respawn_dialog_countdown,
                hours,
                minutes,
                seconds);
        countdownTextView.post(new Runnable() {
            @Override
            public void run() {
                countdownTextView.setText(str);
            }
        });

        if ((remainingMs == 0) && (countdownCompleteRunnable != null)) {
            countdownCompleteRunnable.run();
        }
    }

    public long getRemainingMs() {
        return remainingMs;
    }
}
