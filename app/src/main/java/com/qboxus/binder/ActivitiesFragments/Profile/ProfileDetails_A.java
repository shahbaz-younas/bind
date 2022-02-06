package com.qboxus.binder.ActivitiesFragments.Profile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.qboxus.binder.SimpleClasses.ContextWrapper;
import com.qboxus.binderstatic.Adapters.ImagesSlidingAdapter;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binder.ActivitiesFragments.Profile.EditProfile.EditProfile_A;
import com.qboxus.binder.R;
import com.google.android.material.tabs.TabLayout;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileDetails_A extends AppCompatActivity implements View.OnClickListener {

    ImageButton moveDownButton, editBtn;

    RelativeLayout usernameLayout;
    ScrollView scrollView;

    List<UserMultiplePhoto> imagesList;

    TextView userNameTxt, userJobTitleTxt, userSchoolTxt,
            userGenderTxt, bottomAge, aboutTv;
    LinearLayout jobLayout, schoolLayout, genderLayout;
    LinearLayout previousImage, nextImage, passionLayout, aboutLayout;
    ChipGroup userPassions;

    String userId;

    SharedPreferences prefs;
    Date c;
    SimpleDateFormat df;
    int currentYear;


    @Override
    protected void attachBaseContext(Context newBase) {
        String[] languageArray = newBase.getResources().getStringArray(R.array.language_code);
        List<String> languageCode = Arrays.asList(languageArray);
        String language = Functions.getSharedPreference(newBase)
                .getString(Variables.selectedLanguage, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && languageCode.contains(language)) {
            Locale newLocale = new Locale(language);
            Context context = ContextWrapper.wrap(newBase, newLocale);
            super.attachBaseContext(context);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            setLanguageLocal();
        }
        setContentView(R.layout.activity_profile_details);

        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        currentYear = Integer.parseInt(df.format(c));

        prefs = getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        initializeViews();

    }


    public void setLanguageLocal(){
        String [] languageArray=getResources().getStringArray(R.array.language_code);
        List <String> languageCode= Arrays.asList(languageArray);
        String language = Functions.getSharedPreference(getApplicationContext())
                .getString(Variables.selectedLanguage,"");

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


    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        usernameLayout = findViewById(R.id.username_layout);

        moveDownButton = findViewById(R.id.move_downbtn);
        moveDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editBtn = findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        Bundle bundle = getIntent().getBundleExtra("");
        if (bundle != null) {
            userId = bundle.getString("user_id");
            editBtn.setVisibility(View.GONE);
        }else {
            userId = Functions.getSharedPreference(this).getString(Variables.uid,"");
        }

        imagesList =new ArrayList<>();

        YoYo.with(Techniques.BounceInDown)
                .duration(800)
                .playOn(moveDownButton);

        previousImage = findViewById(R.id.previousImage);
        previousImage.setOnClickListener(this);
        nextImage = findViewById(R.id.nextImage);
        nextImage.setOnClickListener(this);

        userNameTxt = findViewById(R.id.user_name_txt);
        aboutTv = findViewById(R.id.aboutTv);
        bottomAge = findViewById(R.id.bottom_age);
        userJobTitleTxt = findViewById(R.id.user_jobtitle_txt);
        userSchoolTxt = findViewById(R.id.user_school_txt);
        userGenderTxt = findViewById(R.id.user_gender_txt);

        jobLayout = findViewById(R.id.job_layout);
        schoolLayout = findViewById(R.id.school_layout);
        genderLayout = findViewById(R.id.gender_layout);
        aboutLayout = findViewById(R.id.aboutLayout);
        passionLayout = findViewById(R.id.passionLayout);
        userPassions = findViewById(R.id.chipGroup);

        callApiShowUserDetail();
    }


    // below two method is used get the user pictures and about text from our server
    private void callApiShowUserDetail() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(ProfileDetails_A.this, ApiLinks.showUserDetail, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseUserInfo(resp);
            }
        });
    }


    public void parseUserInfo(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject userdata = msg.optJSONObject("User");
                JSONArray userImagesArray = msg.optJSONArray("UserImage");
                JSONArray passionsArray = msg.optJSONArray("UserPassion");

                imagesList.clear();
                for(int i = 0; i<userImagesArray.length(); i++){
                    UserMultiplePhoto model = new UserMultiplePhoto();
                    if(i<userImagesArray.length()){
                        model.setImage(userImagesArray.optJSONObject(i).optString("image"));
                        model.setId(userImagesArray.optJSONObject(i).optString("id"));
                        model.setOrderSequence(Integer.parseInt(userImagesArray.optJSONObject(i).optString("order_sequence")));
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

                userNameTxt.setText(userdata.optString("first_name"));
                if(!userdata.optString("dob").equals("0000-00-00")){
                    try {
                        Date date = df.parse(userdata.optString("dob"));
                        int age = Integer.parseInt(df.format(date));
                        bottomAge.setText(" " + (currentYear - age));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                String jobTitle=userdata.optString("job_title");
                String userCompany=userdata.optString("company");
                String userSchool=userdata.optString("school");
                String userGender=userdata.optString("gender");

                if(userdata.optString("bio").equals("")){
                    aboutLayout.setVisibility(View.GONE);
                }else {
                    aboutLayout.setVisibility(View.VISIBLE);
                    aboutTv.setText(userdata.optString("bio"));
                }

                if(jobTitle.equals("") & !userCompany.equals("")){
                    userJobTitleTxt.setText(userCompany);
                }
                else if(userCompany.equals("") && !jobTitle.equals("") ){
                    userJobTitleTxt.setText(jobTitle);
                }
                else if(userCompany.equals("") && jobTitle.equals("") ){
                    jobLayout.setVisibility(View.GONE);
                }
                else {
                    userJobTitleTxt.setText(jobTitle);
                }

                userSchoolTxt.setText(userdata.optString("school"));
                userGenderTxt.setText(userdata.optString("gender"));

                if(userSchool.equals("")){
                    schoolLayout.setVisibility(View.GONE);
                }

                if(userGender.equals("")){
                    genderLayout.setVisibility(View.GONE);
                }

                if(passionsArray.length()>0){
                    passionLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i<passionsArray.length(); i++){
                        Chip chip1 = (Chip) LayoutInflater.from(ProfileDetails_A.this).inflate(R.layout.item_passion1, null);
                        chip1.setText(passionsArray.optJSONObject(i).optJSONObject("Passion").optString("title"));
                        userPassions.addView(chip1);
                    }
                }
                else {
                    passionLayout.setVisibility(View.GONE);
                }

                setSlider();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private ViewPager mPager;
    ArrayList<String> images;
    public void setSlider(){
        TabLayout indicator = (TabLayout)findViewById(R.id.indicator);
        images=new ArrayList<>();
        for (int i = 0; i < imagesList.size(); i++) {
            UserMultiplePhoto model = imagesList.get(i);
            if(model.getImage() != null && !model.getImage().equals("")){
                images.add(model.getImage());
            }
        }

        mPager = (ViewPager) findViewById(R.id.image_slider_pager);

        try {
            mPager.setAdapter(new ImagesSlidingAdapter(ProfileDetails_A.this,imagesList));
        }
        catch (NullPointerException e){
            e.getCause();
        }
        if(imagesList.size()>1){
            indicator.setVisibility(View.VISIBLE);
        }else{
            indicator.setVisibility(View.GONE);
        }
        mPager.setCurrentItem(0);

        indicator.setupWithViewPager(mPager, true);

    }


    // open the view of Edit profile where 6 pic is visible
    public void editProfile(){
        startActivity(new Intent(ProfileDetails_A.this, EditProfile_A.class));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousImage:
                if (mPager != null && mPager.getChildCount() > 1) {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                }
                break;

            case R.id.nextImage:
                if (mPager != null && mPager.getChildCount() > 1) {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
                break;
        }
    }
}
