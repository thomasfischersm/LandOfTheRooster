package com.playposse.landoftherooster.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.data.BuildingTypeRepository;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostRespawnBattleBuildingEvent;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;

import java.util.Arrays;

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
        setReloadBusinessEvents(Arrays.<Class<? extends BusinessEvent>>asList(
                PostRespawnBattleBuildingEvent.class));
    }

    public static BuildingNeedsToRespawnDialogFragment newInstance(long buildingId) {
        BuildingNeedsToRespawnDialogFragment fragment = new BuildingNeedsToRespawnDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_ARG, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Long readArguments(Bundle savedInstanceState) {
        buildingId = getArguments().getLong(BUILDING_ID_ARG);
        return buildingId;
    }

    @Override
    protected void doInBackground(Context appContext) {
        RoosterDao dao = RoosterDatabase.getInstance(getActivity()).getDao();
        BuildingWithType buildingWithType =
                BuildingTypeRepository.get(dao).queryBuildingWithType(buildingId);
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
