package com.playposse.landoftherooster.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.playposse.landoftherooster.services.BuildingDiscoveryService;

/**
 * An {@link Activity} that has no visual part and simply serves as a target from the notification
 * action to stop the game from running. It closes the foreground service.
 */
public class StopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stopService(new Intent(this, BuildingDiscoveryService.class));
        finishAffinity();
    }
}
