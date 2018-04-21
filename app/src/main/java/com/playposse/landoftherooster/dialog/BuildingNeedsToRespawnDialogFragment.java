package com.playposse.landoftherooster.dialog;

import android.os.Bundle;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.services.broadcastintent.BuildingNeedsToRespawnBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;

import butterknife.BindView;

/**
 * A dialog that tells the user that the building needs to respawn before the user can attack it
 * again. There is a clock shown.
 */
public class BuildingNeedsToRespawnDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = BuildingNeedsToRespawnDialogFragment.class.getSimpleName();

    private static final String BUILDING_ID_ARG = "buildingId";
    private static final String REMAINING_MS_ARG = "remainingMs";

    private long buildingId;
    private long remainingMs;

    @BindView(R.id.countdown_text_view) TextView countdownTextView;

    public BuildingNeedsToRespawnDialogFragment() {
        super(R.layout.dialog_building_needs_to_respawn);

        setDisappearOnDistance(true);
    }

    public static BuildingNeedsToRespawnDialogFragment newInstance(
            RoosterBroadcastIntent roosterIntent) {

        BuildingNeedsToRespawnBroadcastIntent intent =
                (BuildingNeedsToRespawnBroadcastIntent) roosterIntent;
        long buildingId = intent.getBuildingId();
        long remainingMs = intent.getRemainingMs();

        BuildingNeedsToRespawnDialogFragment fragment = new BuildingNeedsToRespawnDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);
        args.putLong(REMAINING_MS_ARG, remainingMs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(REMAINING_MS_ARG)) {
            remainingMs = savedInstanceState.getLong(REMAINING_MS_ARG);
        } else {
            remainingMs = getArguments().getLong(REMAINING_MS_ARG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(REMAINING_MS_ARG, getCountdownRemainingMs());
    }

    @Override
    protected void doInBackground() {
        // Nothing to do.
    }

    @Override
    protected void onPostExecute() {
        startCountdown(null, countdownTextView, remainingMs);
        // TODO: Make the fragment remember the remainingMs on rotation!
    }

    @Override
    protected void onCountdownComplete() {
        dismiss();

        BattleAvailableDialogFragment.newInstance(buildingId)
                .show(getFragmentManager(), null);
    }
}
