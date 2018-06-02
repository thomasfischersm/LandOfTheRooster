package com.playposse.landoftherooster.dialog;

import android.os.Bundle;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import butterknife.BindView;

import static com.playposse.landoftherooster.GameConfig.BATTLE_RESPAWN_DURATION;

/**
 * A dialog that tells the user that the building needs to respawn before the user can attack it
 * again. There is a clock shown.
 */
public class BuildingNeedsToRespawnDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = BuildingNeedsToRespawnDialogFragment.class.getSimpleName();

    private static final String BUILDING_ID_ARG = "buildingId";

    private long buildingId;
    private long remainingMs;

    @BindView(R.id.countdown_text_view) TextView countdownTextView;

    public BuildingNeedsToRespawnDialogFragment() {
        super(R.layout.dialog_building_needs_to_respawn);

        setDisappearOnDistance(true);
        setShowReturnToMapButton(true);
    }

    public static BuildingNeedsToRespawnDialogFragment newInstance(long buildingId) {
        BuildingNeedsToRespawnDialogFragment fragment = new BuildingNeedsToRespawnDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);
    }

    @Override
    protected void doInBackground() {
        RoosterDao dao = RoosterDatabase.getInstance(getActivity()).getDao();
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);
        long lastConquestMs = buildingWithType.getBuilding().getLastConquest().getTime();
        remainingMs = lastConquestMs + BATTLE_RESPAWN_DURATION - System.currentTimeMillis();
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
