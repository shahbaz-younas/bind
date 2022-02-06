package com.qboxus.binder.ActivitiesFragments.Accounts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.binder.Adapters.ProfilePhotosAdapter;
import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.qboxus.binder.SimpleClasses.GpsUtils;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binder.Models.SexualOrientationModel;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binder.R;
import com.qboxus.binderstatic.SimpleClasses.Functions;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.qboxus.binderstatic.SimpleClasses.Variables.SELECT_IMAGE_FROM_GALLRY_CODE;


public class AddPhotos extends Fragment {

    Context context;

    DragRecyclerView profilePhotoList;
    List<UserMultiplePhoto> imagesList;
    ProfilePhotosAdapter profilePhotosAdapter;

    RelativeLayout continueButton;
    TextView continueTv;

    String imageBas64;
    List<String> selectedImagesList = new ArrayList<>();
    UserMultiplePhoto from_image_model;
    UserMultiplePhoto to_image_model;
    int toPos,fromPos;
    int currentPosition=0;
    View view;

    Date c;
    SimpleDateFormat df;
    int currentYear;


    public AddPhotos() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_photos, container, false);
        context = getActivity();

        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        currentYear = Integer.parseInt(df.format(c));


        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                        Signup_A.pager.getCurrentItem(),
                        Signup_A.pager.getOffscreenPageLimit()));
                Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()-1);
            }
        });

        profilePhotoList =view.findViewById(R.id.Profile_photos_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        profilePhotoList.setLayoutManager(layoutManager);
        profilePhotoList.setHasFixedSize(false);

        imagesList = new ArrayList<>();
        for (int i=0; i<6; i++){
            imagesList.add(new UserMultiplePhoto());
        }

        profilePhotosAdapter = new ProfilePhotosAdapter(getContext(), imagesList, true, new ProfilePhotosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserMultiplePhoto item, int postion, View view) {
                currentPosition = postion;
                switch (view.getId()){
                    case R.id.add_btn:
                        selectImage();
                        break;

                    case R.id.cross_btn:
                        for (int j = 0; j<selectedImagesList.size(); j++){
                            if(item.getImage().equals(selectedImagesList.get(j))){
                                selectedImagesList.remove(j);
                                break;
                            }
                        }
                        imagesList.remove(postion);
                        imagesList.add(new UserMultiplePhoto());
                        profilePhotosAdapter.notifyDataSetChanged();
                        if(selectedImagesList.size()>1){
                            continueButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_pink_background));
                            continueTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                        }else{
                            continueButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_google_background));
                            continueTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
                        }
                        break;
                }
            }
        });

        profilePhotosAdapter.setOnItemDragListener(new SimpleDragListener() {
            @Override
            public void onDrop(int fromPosition, int toPosition) {
                super.onDrop(fromPosition, toPosition);
                from_image_model = imagesList.get(fromPosition);
                to_image_model = imagesList.get(toPosition);

                toPos = toPosition;
                fromPos = fromPosition;

                if(to_image_model.getImage() == null || from_image_model.getImage() == null){
                    imagesList.remove(toPosition);
                    imagesList.add(toPosition, from_image_model);

                    imagesList.remove(from_image_model);
                    imagesList.add(fromPosition, to_image_model);
                }
                profilePhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSwiped(int pos) {
                super.onSwiped(pos);
            }
        });

        profilePhotoList.setAdapter(profilePhotosAdapter);


        continueButton = view.findViewById(R.id.continueButton);
        continueTv = view.findViewById(R.id.continue_tv);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImagesList.size() > 1){
                    callApiRegisterUser();
                }
            }
        });

        return view;
    }

    private void callApiRegisterUser() {
        JSONObject parameters = new JSONObject();
        try {
            if(Signup_A.userModel.isSocialLogin){
                parameters.put("social", ""+Signup_A.userModel.socail_type);
                parameters.put("social_id", ""+Signup_A.userModel.socail_id);
                parameters.put("auth_token", ""+Signup_A.userModel.auth_tokon);
                parameters.put("email", "" + Signup_A.userModel.email);
            }else if(!Signup_A.userModel.isFromPh){
                parameters.put("email", "" + Signup_A.userModel.email);
                parameters.put("password", "" + Signup_A.userModel.password);
            }

            parameters.put("phone", "" + Signup_A.userModel.phone_no);
            parameters.put("dob", "" + Signup_A.userModel.date_of_birth);
            parameters.put("first_name", "" + Signup_A.userModel.fname);
            parameters.put("last_name", "");
            parameters.put("username", "" + Signup_A.userModel.fname);
            parameters.put("gender", "" + Signup_A.userModel.gender);
            parameters.put("show_gender", "" + Signup_A.userModel.show_gender);
            parameters.put("show_me_gender", "" + Signup_A.userModel.show_me_gender);
            JSONArray sexualOrientation = new JSONArray();
            if(Signup_A.userModel.orientationList.size()>0){
                for (int i = 0; i<Signup_A.userModel.orientationList.size(); i++){
                    SexualOrientationModel model = Signup_A.userModel.orientationList.get(i);
                    JSONObject object = new JSONObject();
                    object.put("sexual_orientation_id", model.getId());
                    sexualOrientation.put(object);
                }
            }
            parameters.put("user_sexual_orientation", sexualOrientation);
            parameters.put("show_orientation", "" + Signup_A.userModel.show_orientation);
            parameters.put("school_id", "" + Signup_A.userModel.mySchoolId);
            JSONArray passion = new JSONArray();
            if(Signup_A.userModel.userPassion.size()>0){
                for (int i = 0; i<Signup_A.userModel.userPassion.size(); i++){
                    JSONObject object = new JSONObject();
                    object.put("passion_id",Signup_A.userModel.userPassion.get(i));
                    passion.put(object);
                }
            }
            parameters.put("user_passion", passion);

            JSONArray userPhotos = new JSONArray();
            if(imagesList.size()>0){
                for (int i = 0; i<imagesList.size(); i++){
                    UserMultiplePhoto model = imagesList.get(i);
                    if(model.getImage() != null && !model.getImage().isEmpty()){
                        JSONObject object = new JSONObject();
                        object.put("order_sequence", ""+model.getOrderSequence());
                        object.put("image", model.getImage());
                        userPhotos.put(object);
                    }
                }
            }
            parameters.put("user_images", userPhotos);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        ApiRequest.callApi(context, ApiLinks.registerUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseSignUpData(resp);
            }
        });
    }


    // open the gallery when user press button to upload a picture
    private void selectImage() {
        if(Functions.checkPermissions(getActivity())){
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            resultCallback.launch(intent);
        }
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        beginCrop(selectedImage);
                    }
                }
            });


    // bottom there function are related to crop the image
    private void beginCrop(Uri source) {
        Intent intent=CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1).getIntent(requireActivity());
        cropResultCallback.launch(intent);
    }

    ActivityResultLauncher<Intent> cropResultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        CropImage.ActivityResult result1 = CropImage.getActivityResult(data);
                        handleCrop(result1.getUri());
                    }
                }
            });


    private void handleCrop( Uri userImageUri) {
        InputStream imageStream = null;
        try {
            imageStream = requireActivity().getContentResolver().openInputStream(userImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);

        String path = userImageUri.getPath();
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

        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        imageBas64 = Functions.bitmapToBase64(getActivity(), rotatedBitmap);
        selectedImagesList.add(imageBas64);
        for(int i = 0; i<imagesList.size(); i++){
            UserMultiplePhoto model = imagesList.get(i);
            if(imagesList.get(currentPosition).getImage() != null && !imagesList.get(currentPosition).getImage().equals("")){
                imagesList.remove(i);
                model.setImage(imageBas64);
                model.setOrderSequence(i);
                imagesList.add(i,model);
                break;
            }else if(model.getImage() == null || model.getImage().equals("")){
                imagesList.remove(i);
                model.setImage(imageBas64);
                model.setOrderSequence(i);
                imagesList.add(i,model);
                break;
            }
        }

        if(selectedImagesList.size()>1){
            continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pink_background));
            continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }else{
            continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_google_background));
            continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
        }

        profilePhotosAdapter.notifyDataSetChanged();
    }

    public void parseSignUpData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONObject userdata = jsonObject.optJSONObject("msg").optJSONObject("User");
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Variables.prefName, Context.MODE_PRIVATE);
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

                editor.putBoolean(Variables.showMeOnTinder, userdata.optString("hide_me").equals("0"));

                editor.putBoolean(Variables.hideAge, !userdata.optString("hide_age").equals("0"));

                editor.putBoolean(Variables.hide_distance, !userdata.optString("hide_location").equals("0"));

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
                editor.apply();

                gpsStatus();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void gpsStatus() {
        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Functions.cancelLoader();
        if (!gpsStatus) {
            new GpsUtils(getActivity()).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {

                }
            });
        } else if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        }else{
            startActivity(new Intent(getActivity(), MainMenuActivity.class));
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            getActivity().finishAffinity();
        }
    }

    private void enableLocation() {
        startActivity(new Intent(getActivity(), EnableLocation_A.class));
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        getActivity().finishAffinity();
    }
}