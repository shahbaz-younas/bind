package com.qboxus.binderstatic.SimpleClasses;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.R;
import com.gmail.samehadar.iosdialog.CamomileSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by qboxus on 10/20/2018.
 */
public class Functions {

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static SharedPreferences getSharedPreference(Context context){
        if(Variables.sharedPreferences!=null)
            return Variables.sharedPreferences;
        else {
            Variables.sharedPreferences = context.getSharedPreferences(Variables.prefName, Context.MODE_PRIVATE);
            return Variables.sharedPreferences;
        }

    }

    public static int convertDpToPx(Context context, int dp) {
        return (int) ((int) dp * context.getResources().getDisplayMetrics().density);
    }

    public static Dialog dialog;
    public static void showLoader(Context context, boolean outside_touch, boolean cancelAble) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_dialog_loading_view);

        CamomileSpinner loader=dialog.findViewById(R.id.loader);
        loader.start();

        if(!outside_touch) {
            dialog.setCanceledOnTouchOutside(false);
        }

        if(!cancelAble) {
            dialog.setCancelable(false);
        }

        dialog.show();

    }

    public static void cancelLoader(){
        if(dialog!=null){
            dialog.cancel();
        }
    }

    public static String convertSeconds(int seconds){
        int h = seconds/ 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        String sh = (h > 0 ? String.valueOf(h) + " " + "h" : "");
        String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + " " + "min") : "");
        String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + " " + "sec");
        return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "")+ss;
    }

    public static String getRandomString(int n) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int)(alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    public static void showAlert(Context context, String title, String description, Callback callBack){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.DialogStyle);
        builder.setTitle(title);
        builder.setMessage(description);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(callBack!=null){
                    callBack.response("OK");
                }
            }
        });
        builder.create();
        builder.show();
    }


    static BroadcastReceiver broadcastReceiver;
    static IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    public static void unRegisterConnectivity(Context mContext) {
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void registerConnectivity(Context context, final Callback callback) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isConnectedToInternet(context)) {
                    callback.response("connected");
                } else {
                    callback.response("disconnected");
                }
            }
        };

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static Boolean isConnectedToInternet(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            } else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }

        Log.i("update_statut","Network is available : FALSE ");
        return false;

    }

    // this is the delete message dialog which will show after long press in chat message
    public static void showOptions(Context context, CharSequence[] options, final  Callback callback) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle(null);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                callback.response("" + options[item]);
            }
        });

        builder.show();
    }

    public static String bitmapToBase64(Activity activity, Bitmap imageBitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] byteArray = baos .toByteArray();
        String base64= Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    public static void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(context.getExternalFilesDir(null).getAbsolutePath() , "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName+".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkPermissions(Activity context) {
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            Dialog dialog = new Dialog(context);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.item_permission_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            final RelativeLayout okBtn = dialog.findViewById(R.id.okBtn);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.requestPermissions(PERMISSIONS, 2);
                    }
                    dialog.cancel();
                }
            });
            dialog.show();
        } else {
            return true;
        }

        return false;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double calculateSegmentProgress(double currentSegment, double totalSegments){
        return Math.round((currentSegment / totalSegments) * 100);
    }

    public static String changemiletoKm(String mile){
        try {
            int mile_value = Integer.parseInt("mile");
            int km = (int) (mile_value * 1.6);
            return "" + km;
        }
        catch (Exception e){
            return mile;
        }

    }

    public static String convertMillisecondsToHoursMinutes(long milliseconds){
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

}