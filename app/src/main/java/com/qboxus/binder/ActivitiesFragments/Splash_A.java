package com.qboxus.binder.ActivitiesFragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;

import com.qboxus.binder.ActivitiesFragments.Accounts.EnableLocation_A;
import com.qboxus.binder.ActivitiesFragments.Accounts.Login_A;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binder.R;
import com.qboxus.binder.SimpleClasses.ContextWrapper;
import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Splash_A extends AppCompatActivity{

    SharedPreferences sharedPreferences;

    Handler maxHandler;
    Runnable maxRunable;

    @Override
    protected void attachBaseContext(Context newBase) {

        String[] languageArray = newBase.getResources().getStringArray(R.array.language_code);
        List<String> languageCode = Arrays.asList(languageArray);
        sharedPreferences = newBase.getSharedPreferences(Variables.prefName, MODE_PRIVATE);
        String language = sharedPreferences.getString(Variables.selectedLanguage, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && languageCode.contains(language)) {
            Locale newLocale = new Locale(language);
            Context context = ContextWrapper.wrap(newBase, newLocale);
            super.attachBaseContext(context);
        } else {
            super.attachBaseContext(newBase);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            setLanguageLocal();
        }

        sharedPreferences=getSharedPreferences(Variables.prefName,MODE_PRIVATE);
        // here we check the user is already login or not
        new Handler().postDelayed(new Runnable() {
                public void run() {

                    if (sharedPreferences.getBoolean(Variables.islogin, false)) {
                        // if user is already login then we get the current location of user
                        if(getIntent().hasExtra("action_type")){
                            Intent intent= new Intent(Splash_A.this, MainMenuActivity.class);
                            String action_type=getIntent().getExtras().getString("action_type");
                            String receiverId=getIntent().getExtras().getString("senderid");
                            String title=getIntent().getExtras().getString("title");
                            String icon=getIntent().getExtras().getString("icon");

                            intent.putExtra("icon", icon);
                            intent.putExtra("action_type", action_type);
                            intent.putExtra("receiverid", receiverId);
                            intent.putExtra("title", title);

                            startActivity(intent);
                            finish();
                        } else {
                            gpsStatus();
                        }

                    } else {
                        // else we will move the user to login screen
                        startActivity(new Intent(Splash_A.this, Login_A.class));
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        finish();
                    }
                }
            }, 2000);

        getScreenSize();


        maxHandler =new Handler();
        maxRunable =new Runnable() {
            @Override
            public void run() {
                if (sharedPreferences.getString(Variables.currentLat, "").equals("") || sharedPreferences.getString(Variables.currentLon, "").equals("")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Variables.currentLat, Variables.defaultLat);
                    editor.putString(Variables.currentLon, Variables.defaultLon);
                    editor.commit();
                }

                startActivity(new Intent(Splash_A.this, MainMenuActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();
            }
        };
        maxHandler.postDelayed(maxRunable,15000);
    }


    public void setLanguageLocal(){
        String [] languageArray=getResources().getStringArray(R.array.language_code);
        List<String> languageCode= Arrays.asList(languageArray);

        String language=sharedPreferences.getString(Variables.selectedLanguage,"");

        if(languageCode.contains(language)) {
            Locale myLocale = new Locale(language);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            onConfigurationChanged(conf);
        }
    }


    // get the Gps status to check that either a mobile gps is on or off
    public void gpsStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsStatus) {
            enableLocation();
        }else {
            // if on then get the location of the user and save the location into the local database
            getCurrentLocation();
        }
    }


    // if the Gps is successfully on then we will on the again check the Gps status
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2){
            gpsStatus();
        }
    }


    public void getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Variables.screenHeight = displayMetrics.heightPixels;
        Variables.screenWidth = displayMetrics.widthPixels;
    }


    // if user does not permit the app to get the location then we will go to the enable location screen to enable the location permission
    private void enableLocation() {
        startActivity(new Intent(this, EnableLocation_A.class));
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finishAffinity();
    }



    private FusedLocationProviderClient mFusedLocationClient;
    private void getCurrentLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // here if user did not give the permission of location then we move user to enable location screen
            enableLocation();
            return;
        }
        createLocationRequest();
    }


    public void goNext(Location location){
        if (location != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Variables.currentLat, "" + location.getLatitude());
            editor.putString(Variables.currentLon, "" + location.getLongitude());
            editor.commit();
            startActivity(new Intent(Splash_A.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finish();
        } else {
            // else we will use the basic location
            if (sharedPreferences.getString(Variables.currentLat, "").equals("") || sharedPreferences.getString(Variables.currentLon, "").equals("")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.currentLat, Variables.defaultLat);
                editor.putString(Variables.currentLon, Variables.defaultLon);
                editor.commit();
            }
            startActivity(new Intent(Splash_A.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finish();
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

        locationCallback= new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        goNext(location);
                        stopLocationUpdates();

                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,locationCallback
                , Looper.myLooper());

    }


    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }


    @Override
    public void onDestroy() {
        if (mFusedLocationClient!=null && locationCallback!=null) {
            stopLocationUpdates();
        }

        if(maxHandler !=null && maxRunable !=null){
            maxHandler.removeCallbacks(maxRunable);
        }

        super.onDestroy();
    }

}
