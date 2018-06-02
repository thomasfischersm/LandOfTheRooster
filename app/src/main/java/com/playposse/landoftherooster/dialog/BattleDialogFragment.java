package com.playposse.landoftherooster.dialog;

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
import com.playposse.landoftherooster.contentprovider.business.BusinessDataCache;
import com.playposse.landoftherooster.contentprovider.business.BusinessEngine;
import com.playposse.landoftherooster.contentprovider.business.BusinessEvent;
import com.playposse.landoftherooster.contentprovider.business.BusinessEventListener;
import com.playposse.landoftherooster.contentprovider.business.event.consequenceTriggered.PostBattleEvent;
import com.playposse.landoftherooster.contentprovider.business.event.userTriggered.InitiateBattleEvent;
import com.playposse.landoftherooster.glide.GlideApp;
import com.playposse.landoftherooster.services.combat.BattleEventParcelable;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A dialog that carries out the battle and shows the result.
 *
 * TODO: Show battle in progress state.
 */
public class BattleDialogFragment extends BaseDialogFragment {

    private static final String BATTLE_SUMMARY_KEY = "battleSummary";
    private static final String BUILDING_ID_KEY = "buildingId";

    private long buildingId;
    private BattleSummaryParcelable battleSummary;
    private BattleCompleteListener battleCompleteListener;

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
    public void onResume() {
        super.onResume();

        battleCompleteListener = new BattleCompleteListener();
        BusinessEngine.get()
                .addEventListener(PostBattleEvent.class, battleCompleteListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusinessEngine.get()
                .removeEventListener(PostBattleEvent.class, battleCompleteListener);
        battleCompleteListener = null;
    }

    @Override
    protected void doInBackground() {
        // Check if the battle has already completed. Avoid redoing the battle for screen rotations.
        if (battleSummary != null) {
            return;
        }

        BusinessEngine.get()
                .triggerEvent(new InitiateBattleEvent(buildingId));
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

    /**
     * A {@link BusinessEventListener} that listens to the {@link BusinessEngine} to find out when
     * the battle is complete. Then this dialog can show the battle result.
     */
    private class BattleCompleteListener implements BusinessEventListener {

        @Override
        public void onEvent(BusinessEvent event, BusinessDataCache cache) {
            PostBattleEvent postBattleEvent = (PostBattleEvent) event;
            battleSummary = postBattleEvent.getBattleSummary();

            reload(null);
        }
    }
}
