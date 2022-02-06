package com.qboxus.binder.ActivitiesFragments.Profile.EditProfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.qboxus.binder.Adapters.ProfilePhotosAdapter;
import com.qboxus.binder.SimpleClasses.ContextWrapper;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binder.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wonshinhyo.dragrecyclerview.DragRecyclerView;
import com.wonshinhyo.dragrecyclerview.SimpleDragListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfile_A extends AppCompatActivity implements View.OnClickListener {

    ImageButton backBtn;
    EditText aboutEdit, jobTitleEdit, companyEdit;
    TextView counterTv,passionsTv, schoolTv;
    RadioButton maleBtn, femaleBtn;
    SwitchCompat hideAge, hideDistance;

    String imageBas64,selectedSchoolId="";
    DragRecyclerView profilePhotoList;
    UserMultiplePhoto model = new UserMultiplePhoto();
    UserMultiplePhoto from_image_model;
    UserMultiplePhoto to_image_model;
    int toPos,fromPos;
    int position=0;
    ProfilePhotosAdapter profilePhotosAdapter;

    List<UserMultiplePhoto> imagesList;
    TextView doneTxt, profileNameTxt;
    StringBuilder sb = new StringBuilder();
    ArrayList<String> passionIds = new ArrayList<>();



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
        setContentView(R.layout.activity_edit_profile);

        profileNameTxt = findViewById(R.id.profile_name_txt);
        profileNameTxt.setText(getResources().getString(R.string.about)+" "+
                Functions.getSharedPreference(this).getString(Variables.fName,""));

        imagesList = new ArrayList<>();

        profilePhotoList = findViewById(R.id.Profile_photos_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        profilePhotoList.setLayoutManager(layoutManager);
        profilePhotoList.setHasFixedSize(false);

        aboutEdit = findViewById(R.id.about_user);
        counterTv = findViewById(R.id.textCounterTv);
        aboutEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    counterTv.setText(""+(500 - s.length()));
                }
            }
        });

        passionsTv = findViewById(R.id.passionsTv);
        passionsTv.setOnClickListener(this);
        jobTitleEdit = findViewById(R.id.jobtitle_edit);
        companyEdit = findViewById(R.id.company_edit);
        schoolTv = findViewById(R.id.school_edit);
        schoolTv.setOnClickListener(this);
        maleBtn = findViewById(R.id.male_btn);
        femaleBtn = findViewById(R.id.female_btn);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(this);

        doneTxt = findViewById(R.id.done_txt);
        doneTxt.setOnClickListener(this);

        /*hideAge = view.findViewById(R.id.hide_age);
        hideAge.setChecked( Functions.getSharedPreference(context).getBoolean(Variables.hideAge,false));
        hideAge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Functions.getSharedPreference(context).edit().putBoolean(Variables.hideAge,isChecked).commit();
                callApiEditProfile();
            }
        });

        hideDistance =view.findViewById(R.id.hide_distance);
        hideDistance.setChecked( Functions.getSharedPreference(context).getBoolean(Variables.hide_distance,false));
        hideDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Functions.getSharedPreference(context).edit().putBoolean(Variables.hide_distance,isChecked).commit();
                callApiEditProfile();
            }
        });*/



        if(Functions.getSharedPreference(this).getString(Variables.school, "").equals("null")){
            schoolTv.setText(getString(R.string.add_university));
        }else {
            schoolTv.setText(Functions.getSharedPreference(this).getString(Variables.school, ""));
        }

        callApiShowUserDetail();

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


    // open the gallery when user press button to upload a picture
    private void selectImage() {
        if(Functions.checkPermissions(EditProfile_A.this)){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            resultCallback.launch(intent);
        }
    }


    // bottom there function are related to crop the image
    private void beginCrop(Uri source) {
        Intent intent=CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1).getIntent(EditProfile_A.this);
        cropResultCallback.launch(intent);
    }


    private void handleCrop( Uri userimageuri) {
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(userimageuri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

        String path=userimageuri.getPath();
        Matrix matrix = new Matrix();
        android.media.ExifInterface exif = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                exif = new android.media.ExifInterface(path);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                switch (orientation) {
                    case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0,
                imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        imageBas64 = Functions.bitmapToBase64(EditProfile_A.this,rotatedBitmap);

        callApiForAddUserImage(false);

    }


    // after save the image in firebase we will save the image url in our server
    private void callApiForAddUserImage(boolean isChangeSequence) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(getApplicationContext())
                    .getString(Variables.uid,""));

            if(model.getImage() != null && !isChangeSequence){
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("id", model.getId());
                object.put("order_sequence", position);
                object.put("image", imageBas64);
                array.put(object);
                parameters.put("sequence",array);
            }else if(model.getImage() == null && !isChangeSequence){
                parameters.put("image", imageBas64);
                parameters.put("order_sequence_single", position);
            } else if(isChangeSequence){
                JSONArray array = new JSONArray();
                for(int i = 0; i<imagesList.size(); i++){
                    UserMultiplePhoto model = imagesList.get(i);
                    if(model.getImage() != null){
                        JSONObject object = new JSONObject();
                        object.put("id", model.getId());
                        object.put("order_sequence", i);
                        array.put(object);
                    }
                }
                parameters.put("sequence",array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.generateNoteOnSD(EditProfile_A.this, "parameters_uploadImages",parameters.toString());

        Functions.showLoader(EditProfile_A.this,false,false);
        ApiRequest.callApi(EditProfile_A.this, ApiLinks.addUserImage, parameters, resp -> {
            try {
                JSONObject jsonObject=new JSONObject(resp);
                String code = jsonObject.optString("code");
                if(code.equals("200")){
                    callApiShowUserDetail();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

    }


    // this method will call when we click for delete the profile images
    private void callApiForDeleteLink() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("id", model.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(EditProfile_A.this,false,false);
        ApiRequest.callApi(EditProfile_A.this, ApiLinks.deleteUserImage, parameters, resp -> {
           Functions.cancelLoader();
            try {
                JSONObject jsonObject=new JSONObject(resp);
                String code=jsonObject.optString("code");
                if(code.equals("200")){
                    callApiShowUserDetail();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }


    // below two method is used get the user pictures and about text from our server
    private void callApiShowUserDetail() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(EditProfile_A.this)
                    .getString(Variables.uid,""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(Functions.dialog == null){
            Functions.showLoader(EditProfile_A.this,false,false);
        }
        ApiRequest.callApi(EditProfile_A.this, ApiLinks.showUserDetail, parameters, resp -> {
            Functions.cancelLoader();
            parseUserInfo(resp);
        });
    }


    @SuppressLint("NonConstantResourceId")
    public void parseUserInfo(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject userdata = msg.optJSONObject("User");
                JSONObject schoolObject = msg.optJSONObject("School");
                JSONArray userImagesArray = msg.optJSONArray("UserImage");
                JSONArray userPassionsArray = msg.optJSONArray("UserPassion");

                imagesList.clear();
                for(int i = 0; i<6; i++){
                    UserMultiplePhoto model = new UserMultiplePhoto();
                    if(i<userImagesArray.length()){
                        model.setImage(userImagesArray.optJSONObject(i).optString("image"));
                        model.setId(userImagesArray.optJSONObject(i).getString("id"));
                        model.setOrderSequence(Integer.parseInt(userImagesArray
                                .optJSONObject(i).getString("order_sequence")));
                        imagesList.add(i, model);
                    }else {
                        model.setOrderSequence(i);
                        imagesList.add(i, model);
                    }
                }

                Collections.sort(imagesList, (p1, p2) -> {
                    return p1.getOrderSequence() - p2.getOrderSequence(); // Ascending
                });

                if(imagesList.size()>0){
                    Functions.getSharedPreference(EditProfile_A.this).edit()
                            .putString(Variables.uPic, imagesList.get(0).getImage()).apply();
                    MainMenuActivity.userPic = Functions
                            .getSharedPreference(EditProfile_A.this)
                            .getString(Variables.uPic,"");
                }

                aboutEdit.setText(userdata.optString("bio"));
                jobTitleEdit.setText(userdata.optString("job_title"));
                companyEdit.setText(userdata.optString("company"));

                if(schoolObject.optString("name").equals("null")){
                    schoolTv.setText(getString(R.string.add_university));
                }else {
                    schoolTv.setText(schoolObject.optString("name"));
                }

                if(userdata.optString("gender").toLowerCase().equals("male")){
                    maleBtn.setChecked(true);
                }else if(userdata.optString("gender").toLowerCase().equals("female")){
                    femaleBtn.setChecked(true);
                }

                sb = new StringBuilder();
                for(int i =0; i < userPassionsArray.length(); i++) {
                    JSONObject passionObject = userPassionsArray.optJSONObject(i).optJSONObject("Passion");

                    sb.append(passionObject.optString("title"));
                    sb.append(", ");
                }
                if(sb != null && sb.length() > 2){
                    sb.deleteCharAt(sb.length()-2);
                    passionsTv.setText(sb);
                }else {
                    passionsTv.setText(getString(R.string.add_passions));
                }

                profilePhotosAdapter = new ProfilePhotosAdapter(EditProfile_A.this,
                        imagesList, false, (item, postion, view) -> {
                            model = item;
                            position = postion;
                            aboutEdit.clearFocus();
                            companyEdit.clearFocus();
                            jobTitleEdit.clearFocus();
                            Functions.hideSoftKeyboard(EditProfile_A.this);
                            switch (view.getId()){
                                case R.id.cross_btn:
                                    showPopup();
                                    break;

                                case R.id.add_btn:
                                    selectImage();
                                    break;
                            }
                        });

                profilePhotosAdapter.setOnItemDragListener(new SimpleDragListener() {
                    @Override
                    public void onDrop(int fromPosition, int toPosition) {
                        super.onDrop(fromPosition, toPosition);

                        from_image_model = imagesList.get(fromPosition);
                        to_image_model = imagesList.get(toPosition);

                        fromPos = fromPosition;
                        toPos = toPosition;

                        if((to_image_model.getImage() != null && !to_image_model.getImage().equals(""))
                                && (from_image_model.getImage() != null && !from_image_model.getImage().equals(""))){

                            imagesList.remove(toPosition);
                            imagesList.add(toPosition, to_image_model);

                            imagesList.remove(from_image_model);
                            imagesList.add(fromPosition, from_image_model);

                            profilePhotosAdapter.notifyDataSetChanged();

                            callApiForAddUserImage(true);

                        }else {

                            imagesList.remove(toPosition);
                            imagesList.add(toPosition, from_image_model);

                            imagesList.remove(from_image_model);
                            imagesList.add(fromPosition, to_image_model);

                            profilePhotosAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onSwiped(int pos) {
                        super.onSwiped(pos);
                    }
                });
                profilePhotoList.setAdapter(profilePhotosAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // on done btn press this method will call
    // below two method is user for save the change in our profile which we have done
    private void callApiEditProfile() {
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("user_id", Functions.getSharedPreference(EditProfile_A.this)
                    .getString(Variables.uid,""));
            parameters.put("bio", aboutEdit.getText().toString());
            parameters.put("job_title", jobTitleEdit.getText().toString());
            parameters.put("company", companyEdit.getText().toString());
            if(!selectedSchoolId.equals("")){
                parameters.put("school_id", selectedSchoolId);
            }

            if(maleBtn.isChecked()){
                parameters.put("gender","Male");
            }else if(femaleBtn.isChecked()){
                parameters.put("gender","Female");
            }

            JSONArray passion = new JSONArray();
            if(passionIds.size()>0){
                for (int i=0; i<passionIds.size(); i++){
                    JSONObject object = new JSONObject();
                    object.put("passion_id", passionIds.get(i));
                    passion.put(object);
                }
                parameters.put("user_passion", passion);
            }
            /*if(hideAge.isChecked())
                parameters.put("hide_age","1");
            else
                parameters.put("hide_age","0");

            if(hideDistance.isChecked())
                parameters.put("hide_location","1");
            else
                parameters.put("hide_location","0");*/

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(EditProfile_A.this, false, false);
        ApiRequest.callApi(EditProfile_A.this, ApiLinks.editProfile, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseEditData(resp);
            }
        });

    }


    public void parseEditData(String loginData){
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");

            if(code.equals("200")){
                JSONObject userdata = jsonObject.optJSONObject("msg").optJSONObject("User");
                SharedPreferences.Editor editor = Functions
                        .getSharedPreference(EditProfile_A.this).edit();
                editor.putString(Variables.uid, userdata.optString("id"));
                editor.putString(Variables.fName, userdata.optString("first_name"));
                editor.putString(Variables.lName, userdata.optString("last_name"));
                editor.putString(Variables.gender, userdata.optString("gender"));
                editor.putString(Variables.school, ""+jsonObject.optJSONObject("msg")
                        .optJSONObject("School").optString("name"));
                editor.apply();

                finish();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // first step helper function
    private void showPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(EditProfile_A.this,R.style.DialogStyle);
        alert.setMessage(getApplicationContext().getString(R.string.are_you_sure_to_delete_this_picture))
                .setPositiveButton("Delete", (dialog, which)
                        -> callApiForDeleteLink()).setNegativeButton(getApplicationContext().getString(R.string.cancel), null);

        AlertDialog alert1 = alert.create();
        alert1.show();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_btn:
                Functions.hideSoftKeyboard(EditProfile_A.this);
                finish();
                break;

            case R.id.done_txt:
                callApiEditProfile();
                break;

            case R.id.passionsTv:
                Intent intent = new Intent(EditProfile_A.this, AddPassions_A.class);
                Bundle bundle = new Bundle();
                if(sb.length() > 0){
                    bundle.putString(Variables.uPassions, passionsTv.getText().toString());
                }
                intent.putExtra(Variables.uPassions, bundle);
                passionResultCallback.launch(intent);
                break;

            case R.id.school_edit:
                Intent intent1 = new Intent(EditProfile_A.this, AddSchool_A.class);
                Bundle bundle1 = new Bundle();
                if(schoolTv.getText().toString().equals(getString(R.string.add_university))){
                    bundle1.putString(Variables.school, "");
                }else {
                    bundle1.putString(Variables.school, schoolTv.getText().toString());
                }
                intent1.putExtra("school", bundle1);
                schoolResultCallback.launch(intent1);
                break;
        }
    }

    //All Result Callback of Current Activity
    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri selectedImage = data.getData();
                    beginCrop(selectedImage);
                }
            });

    ActivityResultLauncher<Intent> cropResultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    CropImage.ActivityResult result1 = CropImage.getActivityResult(data);
                    handleCrop(result1.getUri());
                }
            });

    ActivityResultLauncher<Intent> passionResultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String passions = data.getStringExtra(Variables.uPassions);
                        if(passions.equals("")){
                            passionsTv.setText(getString(R.string.add_passions));
                            sb = new StringBuilder();
                        }else {
                            passionIds.clear();
                            passionIds = data.getStringArrayListExtra("passionIds");
                            passionsTv.setText(data.getStringExtra(Variables.uPassions));
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> schoolResultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Bundle bundle = data.getBundleExtra("bundle_school");
                        schoolTv.setText(bundle.getString("school_name"));
                        selectedSchoolId = bundle.getString("school_id");
                    }
                }
            });


    @Override
    public void finish() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
