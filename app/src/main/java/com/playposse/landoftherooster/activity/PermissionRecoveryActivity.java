package com.playposse.landoftherooster.activity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.util.PermissionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An {@link Activity} that parks the user until the location permission is granted.
 */
public class PermissionRecoveryActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 3;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.request_permission_layout) LinearLayout requestPermissionLayout;
    @BindView(R.id.grant_location_permission_label) TextView grantLocationPermissionLabel;
    @BindView(R.id.grant_location_permission_link) TextView grantLocationPermissionLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permission_recovery);

        ButterKnife.bind(this);

        initActionBar();

        updateLayoutVisibility();
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateLayoutVisibility() {
        boolean hasFineLocationPermission = PermissionUtil.hasFineLocationPermission(this);

        if (!hasFineLocationPermission) {
            requestPermissionLayout.setVisibility(View.VISIBLE);
            grantLocationPermissionLabel
                    .setVisibility(hasFineLocationPermission ? View.GONE : View.VISIBLE);
            grantLocationPermissionLink
                    .setVisibility(hasFineLocationPermission ? View.GONE : View.VISIBLE);
        } else {
            // All problems are resolved. Send the user back to the camera.
            ActivityNavigator.startKingdomActivity(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        updateLayoutVisibility();
    }

    @OnClick(R.id.grant_location_permission_link)
    void onRequestFineLocationPermissionClicked() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CAMERA_PERMISSION);
    }
}
