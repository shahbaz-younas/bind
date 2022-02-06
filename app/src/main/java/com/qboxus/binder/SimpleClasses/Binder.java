package com.qboxus.binder.SimpleClasses;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binder.R;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Created by qboxus on 10/19/2018.
 */

public class Binder extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Fresco.initialize(this);
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.drawable.image_placeholder) //default: bug image
                .restartActivity(CustomErrorActivity.class) //default: null (your app's launch activity)
                .errorActivity(CustomErrorActivity.class) //default: null (default error activity)
                .eventListener(new CustomEventListener()) //default: null
                .apply();
    }

    private static class CustomEventListener implements CustomActivityOnCrash.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            Log.d(Constants.tag, "onLaunchErrorActivity()");
        }

        @Override
        public void onRestartAppFromErrorActivity() {
            Log.d(Constants.tag, "onRestartAppFromErrorActivity()");
        }

        @Override
        public void onCloseAppFromErrorActivity() {
            Log.d(Constants.tag, "onCloseAppFromErrorActivity()");
        }
    }

}
