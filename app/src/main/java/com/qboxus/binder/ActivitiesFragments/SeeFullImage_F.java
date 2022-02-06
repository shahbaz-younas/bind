package com.qboxus.binder.ActivitiesFragments;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.qboxus.binder.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */

public class SeeFullImage_F extends RootFragment {

    View view;
    Context context;
    ImageButton saveButton, shareButton, closeGallery;
    ImageView singleImage;
    String imageUrl, chatId;
    ProgressBar pBar;
    ProgressDialog progressDialog;

    // this is the third party library that will download the image
    DownloadRequest prDownloader;

    File direct;
    File fullPath;
    int width,height;

    public SeeFullImage_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_see_full_image, container, false);
        context=getContext();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        imageUrl = getArguments().getString("image_url");
        imageUrl = Constants.BASE_URL + imageUrl;

        chatId = getArguments().getString("chat_id");

        closeGallery =view.findViewById(R.id.close_gallery);
        closeGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        progressDialog=new ProgressDialog(context,R.style.AlertDialogCustom);
        progressDialog.setMessage(getActivity().getString(R.string.please_Wait));
        PRDownloader.initialize(getActivity().getApplicationContext());


        // get the full path of image in database
        fullPath = new File(context.getExternalFilesDir(null).getAbsolutePath()  +"/"+context.getResources().getString(R.string.app_name)+"/"+ chatId +".jpg");

        // if the image file is exits then we will hide the save btn
        saveButton =view.findViewById(R.id.savebtn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture(false);
            }
        });
        if(fullPath.exists()){
            saveButton.setVisibility(View.GONE);
        }


        // get  the directory inwhich we want to save the image
        direct = new File(context.getExternalFilesDir(null).getAbsolutePath()  +"/"+context.getResources().getString(R.string.app_name)+"/");

        // this code will download the image
        prDownloader= PRDownloader.download(imageUrl, direct.getPath(), chatId +".jpg").build();
        pBar = view.findViewById(R.id.p_bar);


        singleImage = view.findViewById(R.id.single_image);
        // if the image is already save then we will show the image from directory otherwise
        // we will show the image by using picasso
        if(fullPath.exists()){
            Uri uri= Uri.parse(fullPath.getAbsolutePath());
            singleImage.setImageURI(uri);
        }else {
            pBar.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).placeholder(R.drawable.image_placeholder).into(singleImage, new Callback() {
                @Override
                public void onSuccess() {
                    pBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    pBar.setVisibility(View.GONE);
                }
            });

        }


        shareButton =view.findViewById(R.id.sharebtn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePicture();
            }
        });

        return view;
    }


    // this method will share the picture to other user
    public void sharePicture(){
        if(checkStoragePermission()) {
            Uri bitmapUri;
            if(fullPath.exists()){
                bitmapUri= Uri.parse(fullPath.getAbsolutePath());
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                startActivity(Intent.createChooser(intent, ""));
            } else {
                savePicture(true);
            }
        }
    }


    // this funtion will save the picture but we have to give tht permision to right the storage
    public void savePicture(final boolean isFromShare){
        if(checkStoragePermission()) {
            final File direct = new File(
                    context.getExternalFilesDir(null).getAbsolutePath() + "/DCIM/"+context.getResources().getString(R.string.app_name)+"/");
            progressDialog.show();
            prDownloader.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {

                    MediaScannerConnection.scanFile(getActivity(), new String[] { direct.getPath() + chatId + ".jpg" }, null, new MediaScannerConnection.OnScanCompletedListener() {
                        /*
                         *   (non-Javadoc)
                         * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                         */
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });


                    progressDialog.dismiss();
                    if (isFromShare) {
                        sharePicture();
                    } else {
                         new AlertDialog.Builder(context,R.style.AlertDialogCustom)
                                //set title
                                .setTitle("Image Saved")
                                //set message
                                .setMessage(fullPath.getAbsolutePath())
                                //set negative button
                                .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onError(Error error) {
                    progressDialog.dismiss();
                    Toast.makeText(context, getActivity().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Click Again", Toast.LENGTH_LONG).show();
        }
    }


    public boolean checkStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }else {
            return true;
        }
    }

}


