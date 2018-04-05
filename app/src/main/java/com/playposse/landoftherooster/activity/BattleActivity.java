package com.playposse.landoftherooster.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.services.combat.Battle;
import com.playposse.landoftherooster.services.combat.BattleSummaryParcelable;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link Activity} that carries out a battle.
 */
public class BattleActivity extends AppCompatActivity {

    private static final String BATTLE_SUMMARY_KEY = "battleSummary";

    private BattleSummaryParcelable battleSummary;

    @BindView(R.id.battle_outcome_text_view) TextView battleOutcomeTextView;

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
        } else {
            battleOutcomeTextView.setText(R.string.battle_defeat_msg);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BATTLE_SUMMARY_KEY,battleSummary);
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
}
