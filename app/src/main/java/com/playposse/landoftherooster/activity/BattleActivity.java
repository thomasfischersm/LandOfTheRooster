package com.playposse.landoftherooster.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import butterknife.OnClick;

/**
 * An {@link Activity} that carries out a battle.
 */
public class BattleActivity extends AppCompatActivity {

    private static final String BATTLE_SUMMARY_KEY = "battleSummary";

    private BattleSummaryParcelable battleSummary;

    @BindView(R.id.battle_outcome_text_view) TextView battleOutcomeTextView;
    @BindView(R.id.battle_outcome_image_view) ImageView battleOutcomeImageView;
    @BindView(R.id.battle_info_text_view) TextView battleInfoTextView;
    @BindView(R.id.battle_events_recycler_view) RecyclerView battleEventsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_battle);

        ButterKnife.bind(this);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(BATTLE_SUMMARY_KEY)) {
            battleSummary = savedInstanceState.getParcelable(BATTLE_SUMMARY_KEY);
            showBattleSummary();
        } else {
            new ExecuteBattleAsyncTask().execute();
        }
    }

    private void showBattleSummary() {
        if (battleSummary.isDidFriendsWin()) {
            battleOutcomeTextView.setText(R.string.battle_victory_msg);
            GlideApp.with(this)
                    .load(R.drawable.victory)
                    .into(battleOutcomeImageView);
        } else {
            battleOutcomeTextView.setText(R.string.battle_defeat_msg);
            GlideApp.with(this)
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
        battleEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        BattleEventAdapter adapter = new BattleEventAdapter(battleSummary.getBattleEvents());
        battleEventsRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BATTLE_SUMMARY_KEY, battleSummary);
    }

    @OnClick(R.id.return_to_map_button)
    void onReturnToMapButtonClicked() {
        ActivityNavigator.startKingdomActivity(this);
    }

    /**
     * An {@link AsyncTask} that executes the battle.
     */
    class ExecuteBattleAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = BattleActivity.this;
            int buildingId = ActivityNavigator.getBuildingId(getIntent());
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            BuildingWithType buildingWithType = dao.getBuildingWithTypeByBuildingId(buildingId);

            Battle battle = new Battle(context, buildingWithType);
            battleSummary = battle.fight();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showBattleSummary();
        }
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
            View view = LayoutInflater.from(BattleActivity.this).inflate(
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
