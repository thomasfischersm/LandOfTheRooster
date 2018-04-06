package com.playposse.landoftherooster.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.RoosterDao;
import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;
import com.playposse.landoftherooster.contentprovider.room.entity.Building;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.Unit;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitWithType;
import com.playposse.landoftherooster.dialog.BattleAvailableDialog;
import com.playposse.landoftherooster.dialog.BuildingNeedsToRespawnDialog;
import com.playposse.landoftherooster.services.broadcastintent.BattleAvailableBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.BuildingNeedsToRespawnBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastIntent;
import com.playposse.landoftherooster.services.broadcastintent.RoosterBroadcastManager;
import com.playposse.landoftherooster.util.RecyclerViewLiveDataAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KingdomActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = KingdomActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @BindView(R.id.resource_recycler_view) RecyclerView resourceRecyclerView;
    @BindView(R.id.unit_recycler_view) RecyclerView unitRecyclerView;
    @BindView(R.id.center_button) Button centerButton;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted = false;
    private ResourceAdapter resourceAdapter;
    private UnitAdapter unitAdapter;
    private boolean isUserCentered = false;

    private LocationCallback locationCallback = new ThisLocationCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kingdom);

        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO: This could possibly be removed or reduced.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        new LoadDataAsyncTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RoosterBroadcastManager.getInstance(this)
                .register(new KingdomActivityBroadcastReceiver());
    }

    @Override
    protected void onPause() {
        super.onPause();

        fusedLocationClient.removeLocationUpdates(locationCallback);

        RoosterBroadcastManager.getInstance(this)
                .unregister(new KingdomActivityBroadcastReceiver());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;

                    if (map != null) {
                        onMapReady(map);
                    }
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        getLocationPermission();
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        map.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        onGetInitialLocation(location);
                    }
                });

        startLocationUpdates();

        // Update buildings on the map.
        Observer<List<BuildingWithType>> observer = new Observer<List<BuildingWithType>>() {
            @Override
            public void onChanged(@Nullable List<BuildingWithType> buildingsWithType) {
                // Clear old markers.
                map.clear();

                // Re-draw all markers.
                if (buildingsWithType != null) {
                    for (BuildingWithType buildingWithType : buildingsWithType) {
                        Building building = buildingWithType.getBuilding();
                        BuildingType buildingType = buildingWithType.getBuildingType();

                        int drawableId = getResources().getIdentifier(
                                buildingType.getIcon(),
                                "drawable",
                                getPackageName());

                        Location location = new Location("");
                        location.setLatitude(building.getLatitude());
                        location.setLongitude(building.getLongitude());
                        addMarker(location, drawableId, buildingType.getName());
                    }
                }
            }
        };
        LiveData<List<BuildingWithType>> liveData =
                RoosterDatabase.getInstance(this).getDao().getAllBuildingsWithTypeAsLiveData();
        liveData.observe(this, observer);

        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    onUserMovedTheMap();
                }
            }
        });
    }

    private void onUserMovedTheMap() {
        isUserCentered = true;
        centerButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.center_button)
    void onCenterButtonClicked() {
        isUserCentered = false;
        centerButton.setVisibility(View.GONE);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void onGetInitialLocation(Location location) {
        Log.d(LOG_TAG, "onGetInitialLocation: Got location: " + location);

        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            map.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10_000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private class ThisLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Log.d(LOG_TAG, "onLocationResult: Got new location");
            if (!isUserCentered) {
                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                map.moveCamera(CameraUpdateFactory.zoomTo(17));
            }
        }
    }

    private static LatLng fromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void addMarker(Location location, int drawable, String label) {
        Bitmap bitmap =
                BitmapFactory.decodeResource(getResources(), drawable);
        Bitmap scaledBitmap =
                Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        BitmapDescriptor castleIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

        LatLng latLng = fromLocation(location);
        map.addMarker(new MarkerOptions().position(latLng)
                .title(label)
                .icon(castleIcon));
    }

    /**
     * A {@link RecyclerView.Adapter} that holds resources that the user is currently carrying.
     */
    private class ResourceAdapter
            extends RecyclerViewLiveDataAdapter<ResourceViewHolder, ResourceWithType> {

        private ResourceAdapter(SupportActivity activity, LiveData<List<ResourceWithType>> liveData) {
            super(activity, liveData);
        }

        @NonNull
        @Override
        public ResourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(KingdomActivity.this).inflate(
                    R.layout.list_item_resource,
                    parent,
                    false);
            return new ResourceViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(ResourceViewHolder holder, ResourceWithType data) {
            String str = getString(
                    R.string.resource_listing,
                    data.getType().getName(),
                    data.getResource().getAmount());
            holder.resourceTextView.setText(str);
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} that holds the view for a resource item.
     */
    class ResourceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.resource_text_view) TextView resourceTextView;

        private ResourceViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * A {@link RecyclerView.Adapter} that holds units that the user are currently accompanying the
     * user.
     */
    private class UnitAdapter
            extends RecyclerViewLiveDataAdapter<UnitViewHolder, UnitWithType> {

        private UnitAdapter(SupportActivity activity, LiveData<List<UnitWithType>> liveData) {
            super(activity, liveData);
        }

        @NonNull
        @Override
        public UnitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(KingdomActivity.this).inflate(
                    R.layout.list_item_unit,
                    parent,
                    false);
            return new UnitViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(UnitViewHolder holder, UnitWithType data) {
            Unit unit = data.getUnit();
            UnitType type = data.getType();

            String str = getString(
                    R.string.unit_listing,
                    type.getName(),
                    unit.getHealth(),
                    type.getHealth());
            holder.unitTextView.setText(str);
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} that holds the view for a unit.
     */
    class UnitViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.unit_text_view) TextView unitTextView;

        private UnitViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * A {@link RoosterBroadcastManager.RoosterBroadcastReceiver} that listens for events from
     * the location aware services to interact with the user about them.
     */
    class KingdomActivityBroadcastReceiver
            implements RoosterBroadcastManager.RoosterBroadcastReceiver {

        @Override
        public void onReceive(RoosterBroadcastIntent roosterIntent) {
            if (roosterIntent instanceof BattleAvailableBroadcastIntent) {
                BattleAvailableBroadcastIntent battleAvailableBroadcastIntent =
                        (BattleAvailableBroadcastIntent) roosterIntent;
                BattleAvailableDialog.show(
                        KingdomActivity.this,
                        battleAvailableBroadcastIntent.getBuildingId());
            } else if (roosterIntent instanceof BuildingNeedsToRespawnBroadcastIntent) {
                BuildingNeedsToRespawnBroadcastIntent buildingNeedsToRespawnBroadcastIntent =
                        (BuildingNeedsToRespawnBroadcastIntent) roosterIntent;
                BuildingNeedsToRespawnDialog.show(
                        KingdomActivity.this,
                        buildingNeedsToRespawnBroadcastIntent.getRemainingMs());

            }
        }
    }

    /**
     * An {@link AsyncTask} that loads the data from the database to display in the view.
     */
    class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private LiveData<List<ResourceWithType>> resourcesWithType;
        private LiveData<List<UnitWithType>> unitsWithType;
        private Context context = KingdomActivity.this;

        @Override
        protected Void doInBackground(Void... voids) {
            RoosterDao dao = RoosterDatabase.getInstance(context).getDao();
            resourcesWithType = dao.getAllResourcesWithType();
            unitsWithType = dao.getUnitsWithTypeJoiningUserAsLiveData();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            // Set up resources view.
            resourceRecyclerView.setHasFixedSize(true); // Small performance improvement.
            resourceRecyclerView.setLayoutManager(new LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false));
            resourceAdapter = new ResourceAdapter(KingdomActivity.this, resourcesWithType);
            resourceRecyclerView.setAdapter(resourceAdapter);

            // Set up unit view.
            unitRecyclerView.setHasFixedSize(true); // Small performance improvement.
            unitRecyclerView.setLayoutManager(new LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false));
            unitAdapter = new UnitAdapter(KingdomActivity.this, unitsWithType);
            unitRecyclerView.setAdapter(unitAdapter);
        }
    }
}
