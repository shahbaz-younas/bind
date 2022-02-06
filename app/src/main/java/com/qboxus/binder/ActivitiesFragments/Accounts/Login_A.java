package com.qboxus.binder.ActivitiesFragments.Accounts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.Bundle;

import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.qboxus.binder.SimpleClasses.GpsUtils;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binder.Models.LoginSliderModel;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qboxus.binder.Models.UserModel;
import com.qboxus.binder.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Login_A extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    RelativeLayout gmailLoginLayout, phoneLoginLayout;


    private ArrayList<LoginSliderModel> data_list;
    List<UserMultiplePhoto> imagesList;

    private CallbackManager callbackManager;
    UserModel userModel = new UserModel();

    Date c;
    SimpleDateFormat df;
    int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);

        // if the user is already login through facebook then we will logout the user automatically
        LoginManager.getInstance().logOut();
        sharedPreferences = getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        currentYear = Integer.parseInt(df.format(c));

        imagesList = new ArrayList<>();

        init();

        printKeyHash();

    }


    private ViewPager sliderViewPager;
    private void init() {
        // here is the code of slider which is move in login screen
        // in Account folder the Sliding_Adapter is also belong to this code
        sliderViewPager = findViewById(R.id.pager);
        data_list = new ArrayList<>();
        data_list.add(new LoginSliderModel(getApplicationContext().getString(R.string.login_description_1), "",R.drawable.ic_login_1));
        data_list.add(new LoginSliderModel(getApplicationContext().getString(R.string.login_description_2), "",R.drawable.ic_login_2));
        data_list.add(new LoginSliderModel(getApplicationContext().getString(R.string.login_description_3), "",R.drawable.ic_login_3));
        sliderViewPager.setAdapter(new SlidingAdapter(Login_A.this, data_list));

        TabLayout indicator = (TabLayout) findViewById(R.id.indicator);
        indicator.setupWithViewPager(sliderViewPager, true);

        indicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        gmailLoginLayout = findViewById(R.id.gmail_login_layout);
        gmailLoginLayout.setOnClickListener(v -> {
            Functions.showLoader(Login_A.this, false, false);
            signInWithGmail();
        });

        // this is the login with phone btn and by press this button we will open the Login_phone_A Activity
        phoneLoginLayout = findViewById(R.id.phone_login_layout);
        phoneLoginLayout.setOnClickListener(v -> {
            Email_Phone_F emailPhoneF = new Email_Phone_F("login");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left,
                    R.anim.in_from_left, R.anim.out_to_right);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_model", userModel);
            bundle.putString("fromWhere", "login");
            emailPhoneF.setArguments(bundle);
            transaction.addToBackStack(null);
            transaction.replace(R.id.Login_A, emailPhoneF).commit();
        });

    }

    private void openSignUpFragment(){
        userModel.isFromPh = false;
        userModel.isSocialLogin = true;

        Functions.cancelLoader();
        Intent intent = new Intent(this, Signup_A.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_model", userModel);
        intent.putExtra("bundle",bundle);
        startActivity(intent);

    }

    public void Login(View view) {

        Functions.showLoader(Login_A.this, false, false);

        LoginManager.getInstance().
                logInWithReadPermissions(Login_A.this,
                        Arrays.asList("public_profile", "email"));


        loginWithFB();
    }


    private CallbackManager mCallbackManager;
    //facebook implementation
    public void loginWithFB() {
        // initialize the facebook sdk and request to facebook for login
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(loginResult.getAccessToken() != null){
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }
            }

            @Override
            public void onCancel() {
                // App code
                Functions.cancelLoader();
                Toast.makeText(Login_A.this, getApplicationContext().getString(R.string.login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("resp", "" + error.toString());
                Functions.cancelLoader();
                Toast.makeText(Login_A.this, getApplicationContext().getString(R.string.login_error) + error.toString(), Toast.LENGTH_SHORT).show();
            }

        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }else {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof Email_Phone_F) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }


    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        GraphRequest request = GraphRequest.newMeRequest(token, (user, graphResponse) -> {
            userModel = new UserModel();
            userModel.fname = user.optString("first_name");
            userModel.lname = user.optString("last_name");
            userModel.email = user.optString("email");
            userModel.socail_id = user.optString("id");
            userModel.socail_type = "facebook";
            userModel.auth_tokon = token.getToken();

            callApiForSignUp(getApplicationContext(), token.getToken(),
                    user.optString("id"), "facebook");
        });

        // here is the request to facebook sdk for which type of info we have required
        Bundle parameters = new Bundle();
        parameters.putString("fields", "last_name,first_name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    //google Implementation
    GoogleSignInClient mGoogleSignInClient;
    public void signInWithGmail() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(Login_A.this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Login_A.this);

        if (account != null) {
            String id = account.getId();
            String firstName = ""+account.getGivenName();
            String lastName = ""+account.getFamilyName();
            String email = account.getEmail();
            String authToken = account.getIdToken();

            userModel = new UserModel();
            userModel.fname = firstName;
            userModel.email = email;
            userModel.lname = lastName;
            userModel.socail_id = id;
            userModel.auth_tokon = authToken;
            userModel.socail_type = "google";

            callApiForSignUp(getApplicationContext(), authToken, id, "google");
        } else {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            resultCallback.launch(signInIntent);
        }

    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }else{
                    Functions.cancelLoader();
                }
            });

    //Relate to google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String id = account.getId();
                String firstName = ""+account.getGivenName();
                String lastName = ""+account.getFamilyName();
                String email = account.getEmail();
                String authToken = account.getIdToken();

                userModel = new UserModel();
                userModel.fname = firstName;
                userModel.email = email;
                userModel.lname = lastName;
                userModel.socail_id = id;
                userModel.auth_tokon = authToken;
                userModel.socail_type = "google";


                callApiForSignUp(getApplicationContext(), authToken, id, "google");
            }
        } catch (ApiException e) {
            Functions.cancelLoader();
            Log.d(Constants.tag, "signInResult:failed code=" + e.getStatusCode());
        }

    }

    // this method will store the info of user to  database
    private void callApiForSignUp(Context context, String authToken, String userId, String socialType) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("social", ""+socialType);
            parameters.put("social_id", ""+userId);
            parameters.put("auth_token", ""+authToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.generateNoteOnSD(context, "parameters_signup", parameters.toString());
        if(Functions.dialog == null){
            Functions.showLoader(Login_A.this, false, false);
        }
        ApiRequest.callApi(this, ApiLinks.registerUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseSignUpData(resp, authToken);
            }
        });

    }


    public void parseSignUpData(String loginData, String authToken) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject userdata = jsonObject.optJSONObject("msg").optJSONObject("User");
                SharedPreferences sharedPreferences = getSharedPreferences(Variables.prefName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.uid, userdata.optString("id"));
                editor.putString(Variables.fName, userdata.optString("first_name"));
                editor.putString(Variables.lName, userdata.optString("last_name"));
                editor.putString(Variables.gender, userdata.optString("gender"));

                JSONArray userImagesArray = jsonObject.optJSONObject("msg").optJSONArray("UserImage");

                imagesList.clear();
                for(int i = 0; i<6; i++){
                    UserMultiplePhoto model = new UserMultiplePhoto();
                    if(i<userImagesArray.length()){
                        model.setImage(userImagesArray.optJSONObject(i).optString("image"));
                        model.setId(userImagesArray.optJSONObject(i).getString("id"));
                        model.setOrderSequence(Integer.parseInt(userImagesArray.optJSONObject(i).getString("order_sequence")));
                        imagesList.add(i, model);
                    }else {
                        model.setOrderSequence(i);
                        imagesList.add(i, model);
                    }
                }

                Collections.sort(imagesList, new Comparator<UserMultiplePhoto>() {
                    @Override public int compare(UserMultiplePhoto p1, UserMultiplePhoto p2) {
                        return p1.getOrderSequence() - p2.getOrderSequence(); // Ascending
                    }
                });

                if(imagesList.size()>0){
                    editor.putString(Variables.uPic, imagesList.get(0).getImage());
                }

                if(!userdata.optString("dob").equals("0000-00-00")){
                    try {
                        Date date = df.parse(userdata.optString("dob"));
                        int age = Integer.parseInt(df.format(date));
                        editor.putString(Variables.birthDay, " " + (currentYear - age));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if(userdata.optString("hide_me").equals("0")){
                    editor.putBoolean(Variables.showMeOnTinder, true);
                }else {
                    editor.putBoolean(Variables.showMeOnTinder, false);
                }

                if(userdata.optString("hide_age").equals("0")){
                    editor.putBoolean(Variables.hideAge, false);
                }else {
                    editor.putBoolean(Variables.hideAge, true);
                }

                if(userdata.optString("hide_location").equals("0")){
                    editor.putBoolean(Variables.hide_distance, false);
                }else {
                    editor.putBoolean(Variables.hide_distance, true);
                }

                editor.putInt(Variables.minAge, 18);
                editor.putInt(Variables.maxAge, 75);
                editor.putString(Variables.school, ""+jsonObject.optJSONObject("msg").optJSONObject("School").optString("name"));

                editor.putBoolean(Variables.userLikeLimit, false);
                editor.putBoolean(Variables.userSuperlikeLimit, false);

                editor.putString(Variables.uTotalBoost,userdata.optString("total_boost"));
                editor.putString(Variables.uBoost,userdata.optString("boost"));
                editor.putString(Variables.uWallet,userdata.optString("wallet"));
                editor.putString(Variables.authToken,userdata.optString("auth_token"));
                editor.putBoolean(Variables.islogin, true);
                editor.commit();


                gpsStatus();
            }else if(code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")){
                Functions.cancelLoader();
                openSignUpFragment();
            } else {
                Toast.makeText(this, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Functions.cancelLoader();
            e.printStackTrace();
        }

    }


    private void enableLocation() {
        startActivity(new Intent(this, EnableLocation_A.class));
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finishAffinity();
    }


    public void gpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Functions.cancelLoader();
        if (!gpsStatus) {
            new GpsUtils(Login_A.this).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {

                }
            });
        } else if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        }else{
            startActivity(new Intent(Login_A.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finishAffinity();
        }
    }


    public void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
