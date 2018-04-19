package com.playposse.landoftherooster.dialog.support;

import android.widget.TextView;

import com.playposse.landoftherooster.R;

/**
 * A {@link Runnable} that updates the countdown clock.
 */
public class CountdownUpdateRunnable implements Runnable {

    private static final int SECOND_IN_MS = 1_000;

    private final TextView countdownTextView;

    private long remainingMs;

    public CountdownUpdateRunnable(TextView countdownTextView, long remainingMs) {
        this.countdownTextView = countdownTextView;
        this.remainingMs = remainingMs;
    }

    @Override
    public void run() {
        // Increment time.
        remainingMs -= SECOND_IN_MS;

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

        // TODO: Start battle dialog if 0 is reached. -> Make more generic for reuse of this class.
    }
}
