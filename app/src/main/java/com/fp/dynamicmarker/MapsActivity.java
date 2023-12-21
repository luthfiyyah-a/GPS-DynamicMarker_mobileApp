package com.fp.dynamicmarker;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected LocationCallback mLocationCallback;
    protected boolean mRequestingLocationUpdates;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mStartUpdatesButton = findViewById(R.id.btn_start);
        mStopUpdatesButton = findViewById(R.id.btn_stop);
        mLastUpdateTimeTextView = findViewById(R.id.tv_last_update);
        mLatitudeTextView = findViewById(R.id.tv_latitude);
        mLongitudeTextView = findViewById(R.id.tv_longitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationCallback();

        mStartUpdatesButton.setOnClickListener(view -> {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
            updateButton();
        });

        mStopUpdatesButton.setOnClickListener(view -> {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
            updateButton();
        });
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;

        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(-7.2819705);
        mCurrentLocation.setLongitude(112.795323);
        updateUI();
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    mCurrentLocation = location;
                    mLastUpdateTime = DateFormat.getTimeInstance().format(mCurrentLocation.getTime());
                }

                updateUI();
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000); // Update location every 10 seconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
            Toast.makeText(this, "Location updates started", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Toast.makeText(this, "Location updates stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateButton() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    protected void requestPermission() {
        String[] permission = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permission, 1);
    }

    @SuppressLint("DefaultLocale")
    private void updateUI() {
        mLatitudeTextView.setText(String.format("lat: %f", mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("long: %f", mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("update: %s", mLastUpdateTime == null ? "00:00:00" : mLastUpdateTime));

        LatLng newLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(newLoc).title("Marker in " + mCurrentLocation.getLatitude() + ":" + mCurrentLocation.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc, 15));
    }

}