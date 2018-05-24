package com.playposse.landoftherooster.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.glide.GlideApp;
import com.playposse.landoftherooster.services.combat.Battle;
import com.playposse.landoftherooster.services.combat.BattleEventParcelable;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A dialog that carries out the battle and shows the result.
 */
public class BattleDialogFragment extends BaseDialogFragment {

    private static final String BATTLE_SUMMARY_KEY = "battleSummary";
    private static final String BUILDING_ID_KEY = "buildingId";

    private long buildingId;
    private BattleSummaryParcelable battleSummary;

    @BindView(R.id.battle_outcome_text_view) TextView battleOutcomeTextView;
    @BindView(R.id.battle_outcome_image_view) ImageView battleOutcomeImageView;
    @BindView(R.id.battle_info_text_view) TextView battleInfoTextView;
    @BindView(R.id.battle_events_recycler_view) RecyclerView battleEventsRecyclerView;

    public BattleDialogFragment() {
        super(R.layout.dialog_battle);

        setShowReturnToMapButton(true);
    }

    public static BattleDialogFragment newInstance(long buildingId) {
        BattleDialogFragment fragment = new BattleDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BUILDING_ID_KEY, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments(Bundle savedInstanceState) {
        if (getArguments() != null) {
            buildingId = getArguments().getLong(BUILDING_ID_KEY);
        }

        if ((savedInstanceState != null) && savedInstanceState.containsKey(BATTLE_SUMMARY_KEY)) {
            battleSummary = savedInstanceState.getParcelable(BATTLE_SUMMARY_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BATTLE_SUMMARY_KEY, battleSummary);
    }

    @Override
    protected void doInBackground() {
        // Check if the battle has already completed. Avoid redoing the battle for screen rotations.
        if (battleSummary != null) {
            return;
        }

        Context context = getActivity();
        RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
        BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);

        Battle battle = new Battle(dao, buildingWithType);
        battleSummary = battle.fight();
    }

    @Override
    protected void onPostExecute() {
        showBattleSummary();
    }

    private void showBattleSummary() {
        if (battleSummary.isDidFriendsWin()) {
            battleOutcomeTextView.setText(R.string.battle_victory_msg);
            GlideApp.with(getActivity())
                    .load(R.drawable.victory)
                    .into(battleOutcomeImageView);
        } else {
            battleOutcomeTextView.setText(R.string.battle_defeat_msg);
            GlideApp.with(getActivity())
                    .load(R.drawable.defeat)
                    .into(battleOutcomeImageView);
        }

        String infoStr = getString(
                R.string.battle_info,
                battleSummary.getEnemyUnitCountLost(),
                battleSummary.getEnemyHealthLost(),
                battleSummary.getFriendlyUnitCountLost(),
                battleSummary.getFriendlyHealthLost());
        battleInfoTextView.setText(infoStr);

        battleEventsRecyclerView.setHasFixedSize(true);
        battleEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        BattleEventAdapter adapter = new BattleEventAdapter(battleSummary.getBattleEvents());
        battleEventsRecyclerView.setAdapter(adapter);
    }

    /**
     * A {@link RecyclerView.Adapter} that shows all the battle events.
     */
    private class BattleEventAdapter extends RecyclerView.Adapter<BattleEventViewHolder> {

        private final List<BattleEventParcelable> battleEvents;

        private BattleEventAdapter(List<BattleEventParcelable> battleEvents) {
            this.battleEvents = battleEvents;
        }

        @NonNull
        @Override
        public BattleEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.list_item_battle_event,
                    parent,
                    false);
            return new BattleEventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BattleEventViewHolder holder, int position) {
            BattleEventParcelable battleEvent = battleEvents.get(position);
            final String msg;
            if (battleEvent.isFriendAttack()) {
                if (battleEvent.getAttack() > battleEvent.getDefense()) {
                    msg = getString(
                            R.string.battle_event_friendly_attack_success,
                            battleEvent.getAttackerUnitName(),
                            battleEvent.getDefenderUnitName(),
                            battleEvent.getAttack(),
                            battleEvent.getDefense(),
                            battleEvent.getDamage(),
                            battleEvent.getArmor(),
                            battleEvent.getRemainingHealth() + battleEvent.getActualDamage(),
                            battleEvent.getRemainingHealth());
                } else {
                    msg = getString(
                            R.string.battle_event_friendly_attack_failure,
                            battleEvent.getAttackerUnitName(),
                            battleEvent.getDefenderUnitName(),
                            battleEvent.getAttack(),
                            battleEvent.getDefense());
                }
            } else {
                if (battleEvent.getAttack() > battleEvent.getDefense()) {
                    msg = getString(
                            R.string.battle_event_enemy_attack_success,
                            battleEvent.getAttackerUnitName(),
                            battleEvent.getDefenderUnitName(),
                            battleEvent.getAttack(),
                            battleEvent.getDefense(),
                            battleEvent.getDamage(),
                            battleEvent.getArmor(),
                            battleEvent.getRemainingHealth() + battleEvent.getActualDamage(),
                            battleEvent.getRemainingHealth());
                } else {
                    msg = getString(
                            R.string.battle_event_enemy_attack_failure,
                            battleEvent.getAttackerUnitName(),
                            battleEvent.getDefenderUnitName(),
                            battleEvent.getAttack(),
                            battleEvent.getDefense());
                }
            }

            holder.eventDescriptionTextView.setText(Html.fromHtml(msg));
        }

        @Override
        public int getItemCount() {
            return battleEvents.size();
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} for a battle event.
     */
    class BattleEventViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.event_description_text_view) TextView eventDescriptionTextView;

        private BattleEventViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
