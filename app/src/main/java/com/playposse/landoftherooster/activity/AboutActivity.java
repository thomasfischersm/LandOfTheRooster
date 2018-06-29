package com.playposse.landoftherooster.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.business.data.UnitTypeRepository;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.util.GridLayoutRowViewHolder;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link Activity} that shows the user information about the app.
 */
public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.my_toolbar) Toolbar toolbar;
    @BindView(R.id.friendly_unit_types_grid_layout) GridLayout friendlyUnitTypesGridLayout;
    @BindView(R.id.enemy_unit_types_grid_layout) GridLayout enemyUnitTypesGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        new LoadAsyncTask(this).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityNavigator.startKingdomActivity(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * An {@link AsyncTask} that loads the unit types to display the stats.
     */
    private static class LoadAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<AboutActivity> aboutActivityWeakReference;

        private List<UnitType> friendlyUnitTypes;
        private List<UnitType> enemyUnitTypes;

        public LoadAsyncTask(AboutActivity aboutActivity) {
            aboutActivityWeakReference = new WeakReference<>(aboutActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AboutActivity aboutActivity = aboutActivityWeakReference.get();
            if (aboutActivity != null) {
                RoosterDao dao = RoosterDatabase.getInstance(aboutActivity).getDao();
                UnitTypeRepository unitTypeRepository = UnitTypeRepository.get(dao);
                friendlyUnitTypes = unitTypeRepository.getFriendlyUnitTypes();
                enemyUnitTypes = unitTypeRepository.getEnemyUnitTypes();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AboutActivity aboutActivity = aboutActivityWeakReference.get();
            if (aboutActivity != null) {
                // Show stats for friendly unit types.
                UnitTypeRowViewHolder friendlyViewHolder =
                        new UnitTypeRowViewHolder(aboutActivity.friendlyUnitTypesGridLayout);
                friendlyViewHolder.apply(friendlyUnitTypes);

                // Show stats for enemy unit types.
                UnitTypeRowViewHolder enemyViewHolder =
                        new UnitTypeRowViewHolder(aboutActivity.enemyUnitTypesGridLayout);
                enemyViewHolder.apply(enemyUnitTypes);
            }
        }
    }

    /**
     * A helper class to populate a single row in the {@link GridLayout}. One row represents a
     * unit type and shows its stats.
     */
    static class UnitTypeRowViewHolder extends GridLayoutRowViewHolder<UnitType> {

        @BindView(R.id.name_text_view) TextView nameTextView;
        @BindView(R.id.attack_text_view) TextView attackTextView;
        @BindView(R.id.defense_text_view) TextView defenseTextView;
        @BindView(R.id.damage_text_view) TextView damageTextView;
        @BindView(R.id.armor_text_view) TextView armorTextView;
        @BindView(R.id.health_text_view) TextView healthTextView;

        private UnitTypeRowViewHolder(GridLayout gridLayout) {
            super(gridLayout, R.layout.list_item_unit_type_stats);

            gridLayout.removeAllViews();
        }

        @Override
        protected void populate(UnitType unitType) {
            nameTextView.setText(unitType.getName());
            attackTextView.setText(Integer.toString(unitType.getAttack()));
            defenseTextView.setText(Integer.toString(unitType.getDefense()));
            damageTextView.setText(Integer.toString(unitType.getDamage()));
            armorTextView.setText(Integer.toString(unitType.getArmor()));
            healthTextView.setText(Integer.toString(unitType.getHealth()));
        }

        @Override
        public void populateHeading() {
            Context context = nameTextView.getContext();
            nameTextView.setText(R.string.about_unit_name_column);
            attackTextView.setText(R.string.about_attack_column);
            defenseTextView.setText(R.string.about_defense_column);
            damageTextView.setText(R.string.about_damage_column);
            armorTextView.setText(R.string.about_armor_column);
            healthTextView.setText(R.string.about_health_column);
        }
    }
}
