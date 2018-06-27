package com.playposse.landoftherooster.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.playposse.landoftherooster.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link Activity} that shows the user information about the app.
 */
public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.my_toolbar) Toolbar toolbar;

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
}
