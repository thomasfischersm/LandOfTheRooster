package com.playposse.landoftherooster.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.playposse.landoftherooster.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A dialog that tells the user that the building needs to respawn before the user can attack it
 * again. There is a clock shown.
 */
public final class BuildingNeedsToRespawnDialog {

    private static final String LOG_TAG = BuildingNeedsToRespawnDialog.class.getSimpleName();

    private BuildingNeedsToRespawnDialog() {}


    public static void show(Context context, long remainingMs) {
        // Inflate layout
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View rootView = layoutInflater.inflate(R.layout.dialog_building_needs_to_respawn, null);
        TextView countdownTextView = rootView.findViewById(R.id.coutndown_text_view);

        final ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();
        CountdownUpdateRunnable countdownTask =
                new CountdownUpdateRunnable(countdownTextView, remainingMs);
        scheduledExecutorService.scheduleAtFixedRate(countdownTask, 1, 1, TimeUnit.SECONDS);

        new AlertDialog.Builder(context)
                .setView(rootView)
                .setPositiveButton(
                        R.string.return_to_map_button_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                scheduledExecutorService.shutdownNow();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    /**
     * A {@link Runnable} that updates the countdown clock.
     */
    private static class CountdownUpdateRunnable implements Runnable {

        private static final int SECOND_IN_MS = 1_000;

        private final TextView countdownTextView;

        private long remainingMs;

        private CountdownUpdateRunnable(TextView countdownTextView, long remainingMs) {
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

            // TODO: Start battle dialog if 0 is reached.
        }
    }
}
