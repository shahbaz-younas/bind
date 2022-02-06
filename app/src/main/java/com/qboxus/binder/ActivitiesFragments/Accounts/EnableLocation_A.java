package com.qboxus.binder.ActivitiesFragments.Accounts;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binder.SimpleClasses.GpsUtils;
import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.qboxus.binder.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnableLocation_A extends AppCompatActivity {

    RelativeLayout enableLocationBtn;
    SharedPreferences sharedPreferences;

    public EnableLocation_A() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_location);

        sharedPreferences = getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        enableLocationBtn = findViewById(R.id.enable_location_btn);
        enableLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
            }
        });

        gpsStatus();
    }


    private void getLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsStatus();
                } else {
                    Toast.makeText(this, this.getString(R.string.please_grant_permission), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void gpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsStatus) {
            new GpsUtils(EnableLocation_A.this).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {

                }
            });

        } else if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        } else {
            getCurrentLocation();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            gpsStatus();
        }
    }


    private FusedLocationProviderClient mFusedLocationClient;
    private void getCurrentLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }


    public void goNext(Location location) {
        if (location != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Variables.currentLat, "" + location.getLatitude());
            editor.putString(Variables.currentLon, "" + location.getLongitude());
            editor.commit();

            startActivity(new Intent(EnableLocation_A.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finishAffinity();

        } else {
            // else we will use the basic location
            if (sharedPreferences.getString(Variables.currentLat, "").equals("") || sharedPreferences.getString(Variables.currentLon, "").equals("")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.currentLat, "33.738045");
                editor.putString(Variables.currentLon, "73.084488");
                editor.commit();
            }

            startActivity(new Intent(EnableLocation_A.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finishAffinity();

        }
    }


    private LocationRequest mLocationRequest;
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(Constants.DISPLACEMENT);

        startLocationUpdates();
    }


    LocationCallback locationCallback;
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(Constants.tag, location.getLatitude() + "--" + location.getLongitude());

                        goNext(location);
                        stopLocationUpdates();

                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback
                , Looper.myLooper());

    }


    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }


    @Override
    public void onDestroy() {
        if (mFusedLocationClient != null && locationCallback != null) {
            stopLocationUpdates();
        }
        super.onDestroy();
    }

}

