package com.qboxus.binder.ActivitiesFragments.Chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.binderstatic.Interfaces.Callback;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.binder.ActivitiesFragments.Chat.Audio.PlayAudio_F;
import com.qboxus.binder.ActivitiesFragments.Chat.Audio.SendAudio;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.Interfaces.FragmentCallback;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binder.ActivitiesFragments.Profile.ProfileDetails_A;
import com.qboxus.binder.ActivitiesFragments.Users.ReportType_F;
import com.qboxus.binder.R;
import com.qboxus.binder.ActivitiesFragments.SeeFullImage_F;
import com.qboxus.binder.ActivitiesFragments.VideoCalling.VideoActivity;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class ChatActivity extends RootFragment implements View.OnClickListener {

    DatabaseReference rootref;
    String senderid = "",userId,userName;
    String receiverid = "";
    String receiverName = "";
    String receiverPic = "null";
    public static String token = "null";
    boolean isMatchExits = false;

    EditText message;

    private DatabaseReference adduserToInbox;

    private DatabaseReference mChatRefReteriving;
    private DatabaseReference sendTypingIndication;
    private DatabaseReference receiveTypingIndication;
    RecyclerView chatRecyclerView;
    TextView userNameTv;
    List<ChatModel> mChats = new ArrayList<>();
    ChatAdapter mAdapter;
    ProgressBar pBar;

    Query queryGetChat;
    Query myBlockStatusQuery;
    Query otherBlockStatusQuery;
    boolean isUserAlreadyBlock = false;

    SimpleDraweeView profileImage;
    public static String senderDdForCheckNotification = "";
    public static String uploadingImageId = "none";

    Context context;
    View view;
    LinearLayout gifLayout;
    ImageButton uploadGifButton;
    ImageView sendButton;
    int wallet;


    public static String uploadingAudioId = "none";
    ImageButton micBtn;

    File direct;
    SendAudio sendAudio;

    FragmentCallback callback;

    public ChatActivity() {
        //Required Empty
    }

    public ChatActivity(FragmentCallback callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_chat, container, false);
        context = getContext();
        userId = Functions.getSharedPreference(context).getString(Variables.uid,"");
        userName = Functions.getSharedPreference(context).getString(Variables.fName,"");
        direct = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + context.getResources().getString(R.string.app_name) + "/");
        wallet = Integer.parseInt(Functions.getSharedPreference(context).getString(Variables.uWallet, "0"));

        // intialize the database refer
        rootref = FirebaseDatabase.getInstance().getReference();
        adduserToInbox = FirebaseDatabase.getInstance().getReference();

        message = (EditText) view.findViewById(R.id.msgedittext);
        userNameTv = view.findViewById(R.id.username);
        profileImage = view.findViewById(R.id.profileimage);

        // the send id and receiver id from the back activity in which we come from
        Bundle bundle = getArguments();
        if (bundle != null) {
            senderid = bundle.getString("Sender_Id");
            receiverid = bundle.getString("Receiver_Id");
            receiverName = bundle.getString("name");

            receiverPic = bundle.getString("picture");

            isMatchExits = bundle.getBoolean("is_match_exits");
            userNameTv.setText(receiverName);
            senderDdForCheckNotification = receiverid;

            // these two method will get other details of user like there profile pic link and username
            String image = receiverPic;
            if (image != null && !image.contains("http")) {
                image = Constants.BASE_URL + receiverPic;
            }

            Uri uri = Uri.parse(image);
            profileImage.setImageURI(uri);

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileDetail();
                }
            });

            sendAudio = new SendAudio(context, message, rootref, adduserToInbox,
                    senderid, receiverid, receiverName, receiverPic, isMatchExits);

            rootref.child("Users").child(receiverid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        token = dataSnapshot.child("token").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        pBar = view.findViewById(R.id.progress_bar);


        //set layout manager to chat recycler view and get all the privous chat of th user which spacifc user
        chatRecyclerView = (RecyclerView) view.findViewById(R.id.chatlist);
        final LinearLayoutManager layout = new LinearLayoutManager(context);
        layout.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layout);
        chatRecyclerView.setHasFixedSize(false);
        OverScrollDecoratorHelper.setUpOverScroll(chatRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        mAdapter = new ChatAdapter(mChats, senderid, context, new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatModel item, View v) {
                if (item.getType().equals("image")) {
                    openFullSizeImage(item);
                }

                if (v.getId() == R.id.audio_bubble) {
                    RelativeLayout mainLayout = (RelativeLayout) v.getParent();

                    File fullPath = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + context.getResources().getString(R.string.app_name) + "/" + item.getChat_id() + ".mp3");
                    if (fullPath.exists()) {
                        openAudio(fullPath.getAbsolutePath());
                    } else {
                        downloadAudio((ProgressBar) mainLayout.findViewById(R.id.p_bar), item);
                    }
                }
            }
        }, new ChatAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(ChatModel item, View view) {
                if (view.getId() == R.id.msgtxt) {
                    if (senderid.equals(item.getSender_id()) && isTodayMessage(item.getTimestamp())) {
                        deleteMessage(item);
                    }
                } else if (view.getId() == R.id.chatimage) {
                    if (senderid.equals(item.getSender_id()) && isTodayMessage(item.getTimestamp())) {
                        deleteMessage(item);
                    }
                } else if (view.getId() == R.id.audio_bubble) {
                    if (senderid.equals(item.getSender_id()) && isTodayMessage(item.getTimestamp())) {
                        deleteMessage(item);
                    }
                }
            }
        });


        chatRecyclerView.setAdapter(mAdapter);
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutItems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollOutItems = layout.findFirstCompletelyVisibleItemPosition();

                if (userScrolled && (scrollOutItems == 0 && mChats.size() > 9)) {
                    userScrolled = false;
                    pBar.setVisibility(View.VISIBLE);
                    rootref.child("chat").child(senderid + "-" + receiverid).orderByChild("chat_id")
                            .endAt(mChats.get(0).getChat_id()).limitToLast(20)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    pBar.setVisibility(View.GONE);
                                    ArrayList<ChatModel> arrayList = new ArrayList<>();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        ChatModel item = parseData(snapshot);
                                        arrayList.add(item);
                                    }

                                    for (int i = arrayList.size() - 2; i >= 0; i--) {
                                        mChats.add(0, arrayList.get(i));
                                    }

                                    mAdapter.notifyDataSetChanged();
                                    if (arrayList.size() > 8) {
                                        chatRecyclerView.scrollToPosition(arrayList.size());
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        gifLayout = view.findViewById(R.id.gif_layout);

        sendButton = view.findViewById(R.id.sendbtn);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(message.getText().toString())) {
                    if (gifLayout.getVisibility() == View.VISIBLE) {
                        searchGif(message.getText().toString());
                    } else {
                        callApiDeleteMatch();
                        sendMessage(message.getText().toString());
                        message.setText(null);
                    }
                }
            }
        });


        uploadGifButton = view.findViewById(R.id.upload_gif_btn);
        uploadGifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gifLayout.getVisibility() == View.VISIBLE) {
                    slideDown();
                } else {
                    slideUp();
                    getGipy();
                }
            }
        });


        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    sendTypingIndicator(false);
                }
            }
        });


        // this is the message field event lister which tells the second user either the user is typing or not
        // most important to show type indicator to second user
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    sendTypingIndicator(false);
                } else {
                    sendTypingIndicator(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // this the mic touch listener
        // when our touch action is Down is will start recording and when our Touch action is Up
        // it will stop the recording
        micBtn = view.findViewById(R.id.mic_btn);
        final long[] touchTime = {System.currentTimeMillis()};
        micBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (checkRecordPermission() && checkWriteStoragePermission()) {
                        touchTime[0] = System.currentTimeMillis();
                        sendAudio.runbeep("start");
                        Toast.makeText(context, getActivity().getString(R.string.recording), Toast.LENGTH_SHORT).show();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    callApiDeleteMatch();
                    if (touchTime[0] + 4000 > System.currentTimeMillis()) {
                        sendAudio.stopTimer();
                        Toast.makeText(context, getActivity().getString(R.string.hold_Mic_Button_to_Record), Toast.LENGTH_SHORT).show();
                    } else {
                        sendAudio.stopRecording();

                        Toast.makeText(context, getActivity().getString(R.string.stop_Recording), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });


        // this method receiver the type indicator of second user to tell that his friend is typing or not
        receiveTypeIndication();

        view.findViewById(R.id.uploadimagebtn).setOnClickListener(this::onClick);
        view.findViewById(R.id.Goback).setOnClickListener(this::onClick);
        view.findViewById(R.id.alert_btn).setOnClickListener(this::onClick);
        view.findViewById(R.id.video_call_btn).setOnClickListener(this);
        view.findViewById(R.id.voice_call_btn).setOnClickListener(this::onClick);

        getChatData();

        return view;
    }


    public void callApiDeleteInbox() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sender_id", userId);
            parameters.put("receiver_id", receiverid);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(context,false,false);
        ApiRequest.callApi(context, ApiLinks.deleteInbox, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                rootref.child("Inbox").child(userId).child(receiverid).removeValue();
                rootref.child("Inbox").child(receiverid).child(userId).removeValue();
                rootref.child("chat").child(userId).child(receiverid).removeValue();
                rootref.child("chat").child(receiverid).child(userId).removeValue();
                callback.responce(new Bundle());
                getActivity().onBackPressed();
            }
        });
    }

    public void openUserReport(String receiverId) {
        ReportType_F reportType = new ReportType_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

        getActivity().getSupportFragmentManager().setFragmentResultListener("1122", 
                this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if(requestKey.equals("1122") && result != null){
                    if(result.getBoolean("check", false)){
                        blockUser();
                        getActivity().onBackPressed();
                    }
                }
            }
        });
        
        Bundle bundle = new Bundle();
        bundle.putString("user_id", receiverId);
        reportType.setArguments(bundle);

        transaction.addToBackStack(null);
        transaction.replace(R.id.Chat_F, reportType).commit();
        onPause();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Goback:
                callback.responce(new Bundle());
                Functions.hideSoftKeyboard(getActivity());
                getActivity().onBackPressed();
                break;

            case R.id.alert_btn:
                blockUserDialog();
                break;

            case R.id.uploadimagebtn:
                selectImage();
                break;

            case R.id.video_call_btn:
                if (!checkPermissionForCameraAndMicrophone()) {
                    requestPermissionForCameraAndMicrophone();
                } else {
                    if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false) || !Constants.CALLING_LIMIT){
                        openCalling("video_call");
                    }else {
                        if(wallet == Constants.VIDEO_CALL_COINS || wallet>Constants.VIDEO_CALL_COINS){
                            double videoCallLimit = wallet/Constants.VIDEO_CALL_COINS;

                            long videoCallLimit2 = (long) (videoCallLimit * 60) * 1000 ;
                            Constants.MAX_VIDEO_CALLING_TIME_CREDITS = (int) videoCallLimit2;

                            String videoCallTime = "minutes";
                            if(videoCallLimit < 60.0){
                                videoCallTime = Math.round(videoCallLimit) + " minutes";
                            }else {
                                String time = Functions.convertMillisecondsToHoursMinutes(videoCallLimit2);
                                String[] strArr = time.split(":");
                                String hours = strArr[0];
                                String minutes = strArr[1];
                                videoCallTime = hours +" "+ "hours";
                                if(!minutes.equals("00")){
                                    videoCallTime = videoCallTime +" "+ minutes+" "+" minutes";
                                }
                            }
                            Functions.showAlert(getActivity(), getString(R.string.alert), "According to your credits we only allow you"+" "+videoCallTime+" "+context.getString(R.string.for_video_Call), new Callback() {
                                @Override
                                public void response(String response) {
                                    openCalling("video_call");
                                }
                            });
                        }else {
                            int videoCallTime = Functions.getSharedPreference(context).getInt(Variables.videoCallingUsedTime, 0);
                            if (videoCallTime < Constants.MAX_VIDEO_CALLING_TIME) {
                                Functions.showAlert(getActivity(), getString(R.string.alert), getActivity().getString(R.string.for_demo_purpose) + Functions.convertSeconds(Constants.MAX_VIDEO_CALLING_TIME / 1000)+" "+context.getString(R.string.for_video_Call), new Callback() {
                                    @Override
                                    public void response(String response) {
                                        openCalling("video_call");
                                    }
                                });
                            } else {
                                Functions.showAlert(getActivity(), getString(R.string.alert), getString(R.string.video_reach_limit), null);
                            }
                        }
                    }
                }
                break;

            case R.id.voice_call_btn:
                if (!checkPermissionForCameraAndMicrophone()) {
                    requestPermissionForCameraAndMicrophone();
                } else {
                    if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false) || !Constants.CALLING_LIMIT){
                        openCalling("voice_call");
                    }else {
                        if(wallet == Constants.AUDIO_CALL_COINS || wallet>Constants.AUDIO_CALL_COINS){
                            double audioCallLimit = wallet/Constants.AUDIO_CALL_COINS;

                            long audioCallLimit2 = (long) (audioCallLimit * 60) * 1000 ;
                            Constants.MAX_AUDIO_CALLING_TIME_CREDITS = (int) audioCallLimit2;

                            String audioCallTime = "minutes";
                            if(audioCallLimit < 60.0){
                                audioCallTime = Math.round(audioCallLimit) + " minutes";
                            }else {
                                String time = Functions.convertMillisecondsToHoursMinutes(audioCallLimit2);
                                String[] strArr = time.split(":");
                                String hours = strArr[0];
                                String minutes = strArr[1];
                                audioCallTime = hours +" "+ "hours";
                                if(!minutes.equals("00")){
                                    audioCallTime = audioCallTime +" "+ minutes+" "+" minutes";
                                }
                            }
                            Functions.showAlert(getActivity(), getString(R.string.alert), "According to your credits we only allow you"+" "+audioCallTime+" "+context.getString(R.string.for_voice_Call), new Callback() {
                                @Override
                                public void response(String response) {
                                    openCalling("voice_call");
                                }
                            });
                        }else {
                            int videoCallTime =  Functions.getSharedPreference(context).getInt(Variables.audioCallingUsedTime, 0);
                            if (videoCallTime < Constants.MAX_AUDIO_CALLING_TIME) {
                                Functions.showAlert(getActivity(), getString(R.string.alert), getActivity().getString(R.string.for_demo_purpose) + Functions.convertSeconds(Constants.MAX_AUDIO_CALLING_TIME / 1000) + context.getString(R.string.for_voice_Call), new Callback() {
                                    @Override
                                    public void response(String response) {
                                        openCalling("voice_call");
                                    }
                                });
                            } else {
                                Functions.showAlert(getActivity(), getString(R.string.alert), getString(R.string.voice_reah_limit), null);
                            }
                        }
                    }
                }
                break;
        }
    }


    public void openCalling(String type) {
        Intent intent2 = new Intent(getActivity(), VideoActivity.class);
        intent2.putExtra("id", receiverid);
        intent2.putExtra("name", receiverName);
        intent2.putExtra("image", receiverPic);
        intent2.putExtra("status", VideoActivity.callSend);
        intent2.putExtra("call_type", type);
        intent2.putExtra("roomname", Functions.getRandomString(10));

        callingCallback.launch(intent2);
    }

    ActivityResultLauncher<Intent> callingCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        int usedCoins = Functions.getSharedPreference(context)
                                .getInt(Variables.usedCoins, 0);
                        String callType = Functions.getSharedPreference(context)
                                .getString(Variables.callType, "");
                        String dialingUserId = Functions.getSharedPreference(context)
                                .getString(Variables.dialingUserId, "");
                        if(usedCoins > 0 && dialingUserId.equals(Functions
                                .getSharedPreference(context).getString(Variables.uid,""))){
                            callApiForUseCoins(usedCoins, callType);
                        }
                    }
                }
            });


    public void callApiDeleteMatch(){
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("other_user_id", receiverid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.deleteMatch, parameters, new Callback() {
            @Override
            public void response(String resp) {
            }
        });
    }

    ValueEventListener valueEventListener;
    ChildEventListener eventListener;
    ValueEventListener myInboxListener;
    ValueEventListener otherInboxListener;

    public void getChatData() {
        mChats.clear();
        mChatRefReteriving = FirebaseDatabase.getInstance().getReference();
        queryGetChat = mChatRefReteriving.child("chat").child(senderid + "-" + receiverid);

        myBlockStatusQuery = mChatRefReteriving.child("Inbox")
                .child(userId)
                .child(receiverid);

        otherBlockStatusQuery = mChatRefReteriving.child("Inbox")
                .child(receiverid)
                .child(userId);


        // this will get all the messages between two users
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    ChatModel model = dataSnapshot.getValue(ChatModel.class);//Parse_data(dataSnapshot);

                    mChats.add(model);
                    mAdapter.notifyItemInserted(mChats.size());
                    chatRecyclerView.scrollToPosition(mChats.size() - 1);

                    pBar.setVisibility(View.GONE);
                } catch (Exception ex) {
                    Log.e(Constants.tag, "Exception" + ex.toString());
                }

                changeStatus();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    try {
                        ChatModel model = dataSnapshot.getValue(ChatModel.class);

                        for (int i = mChats.size() - 1; i >= 0; i--) {
                            if (mChats.get(i).getTimestamp().equals(dataSnapshot.child("timestamp").getValue())) {
                                mChats.remove(i);
                                mChats.add(i, model);
                                break;
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                        Log.e(Constants.tag, ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.tag, databaseError.getMessage());
            }
        };


        // this will check the two user are do chat before or not
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pBar.setVisibility(View.GONE);
                if (dataSnapshot.hasChild(senderid + "-" + receiverid)) {
                    queryGetChat.removeEventListener(valueEventListener);
                } else {
                    queryGetChat.removeEventListener(valueEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        //this will check the block status of user which is open the chat. to know either i am blocked or not
        //if i am block then the bottom Write chat layout will be invisible
        myInboxListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("block").getValue() != null) {
                    String block = dataSnapshot.child("block").getValue().toString();
                    if (block.equals("1")) {
                        view.findViewById(R.id.writechatlayout).setVisibility(View.INVISIBLE);
                    } else {
                        view.findViewById(R.id.writechatlayout).setVisibility(View.VISIBLE);
                    }
                } else {
                    view.findViewById(R.id.writechatlayout).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // this will check the block status of other user and according to them the block status dialog's option will be change
        otherInboxListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("block").getValue() != null) {
                    String block = dataSnapshot.child("block").getValue().toString();
                    if (block.equals("1")) {
                        view.findViewById(R.id.writechatlayout).setVisibility(View.INVISIBLE);
                        isUserAlreadyBlock = true;
                    } else {
                        view.findViewById(R.id.writechatlayout).setVisibility(View.VISIBLE);
                        isUserAlreadyBlock = false;
                    }
                } else {
                    isUserAlreadyBlock = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        queryGetChat.limitToLast(20).addChildEventListener(eventListener);
        mChatRefReteriving.child("chat").addValueEventListener(valueEventListener);

        myBlockStatusQuery.addValueEventListener(myInboxListener);
        otherBlockStatusQuery.addValueEventListener(otherInboxListener);
    }


    public ChatModel parseData(DataSnapshot dataSnapshot) {
        ChatModel model = new ChatModel();
        model.chat_id = dataSnapshot.child("chat_id").getValue().toString();
        model.receiver_id = dataSnapshot.child("receiver_id").getValue().toString();
        model.sender_id = dataSnapshot.child("sender_id").getValue().toString();
        model.sender_name = dataSnapshot.child("sender_name").getValue().toString();
        model.text = dataSnapshot.child("text").getValue().toString();
        model.pic_url = dataSnapshot.child("pic_url").getValue().toString();
        model.status = dataSnapshot.child("status").getValue().toString();
        model.timestamp = dataSnapshot.child("timestamp").getValue().toString();
        model.type = dataSnapshot.child("type").getValue().toString();
        return model;
    }


    // this will add the new message in chat node and update the ChatInbox by new message by present date
    public void sendMessage(final String message) {
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderid + "-" + receiverid;
        final String chat_user_ref = "chat" + "/" + receiverid + "-" + senderid;

        DatabaseReference reference = rootref.child("chat").child(senderid + "-" + receiverid).push();
        final String pushid = reference.getKey();

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverid);
        message_user_map.put("sender_id", senderid);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", message);
        message_user_map.put("type", "text");
        message_user_map.put("pic_url", ""+receiverPic);
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", userName);
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderid + "/" + receiverid;
                String inbox_receiver_ref = "Inbox" + "/" + receiverid + "/" + senderid;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderid);
                sendermap.put("name", userName);
                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.uPic,""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverid);
                receivermap.put("name", receiverName);
                receivermap.put("pic", receiverPic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserToInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(getActivity()!=null){
                            ChatActivity.sendPushNotification(getActivity(), message,
                                    receiverid, senderid);
                        }
                    }
                });
            }
        });
    }


    // this method will upload the image in chhat
    public void uploadImage(ByteArrayOutputStream byteArrayOutputStream) {
        Log.d(Constants.tag, "uploadImage");
        byte[] data = byteArrayOutputStream.toByteArray();
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        StorageReference reference = FirebaseStorage.getInstance().getReference();
        DatabaseReference dref = rootref.child("chat").child(senderid + "-" + receiverid).push();
        final String key = dref.getKey();
        uploadingImageId = key;
        final String current_user_ref = "chat" + "/" + senderid + "-" + receiverid;
        final String chat_user_ref = "chat" + "/" + receiverid + "-" + senderid;

        HashMap my_dummi_pic_map = new HashMap<>();
        my_dummi_pic_map.put("receiver_id", receiverid);
        my_dummi_pic_map.put("sender_id", senderid);
        my_dummi_pic_map.put("chat_id", key);
        my_dummi_pic_map.put("text", "");
        my_dummi_pic_map.put("type", "image");
        my_dummi_pic_map.put("pic_url", "none");
        my_dummi_pic_map.put("status", "0");
        my_dummi_pic_map.put("time", "");
        my_dummi_pic_map.put("sender_name", userName);
        my_dummi_pic_map.put("timestamp", formattedDate);

        HashMap dummy_push = new HashMap<>();
        dummy_push.put(current_user_ref + "/" + key, my_dummi_pic_map);
        rootref.updateChildren(dummy_push);

        final StorageReference imagepath = reference.child("images").child(key + ".jpg");
        imagepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        uploadingImageId = "none";

                        HashMap message_user_map = new HashMap<>();
                        message_user_map.put("receiver_id", receiverid);
                        message_user_map.put("sender_id", senderid);
                        message_user_map.put("chat_id", key);
                        message_user_map.put("text", "");
                        message_user_map.put("type", "image");
                        message_user_map.put("pic_url", uri.toString());
                        message_user_map.put("status", "0");
                        message_user_map.put("time", "");
                        message_user_map.put("sender_name", userName);
                        message_user_map.put("timestamp", formattedDate);

                        HashMap user_map = new HashMap<>();

                        user_map.put(current_user_ref + "/" + key, message_user_map);
                        user_map.put(chat_user_ref + "/" + key, message_user_map);

                        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                String inbox_sender_ref = "Inbox" + "/" + senderid + "/" + receiverid;
                                String inbox_receiver_ref = "Inbox" + "/" + receiverid + "/" + senderid;

                                HashMap sendermap = new HashMap<>();
                                sendermap.put("rid", senderid);
                                sendermap.put("name", userName);
                                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.uPic,""));
                                sendermap.put("msg", "Send an image");
                                sendermap.put("status", "0");
                                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                                sendermap.put("date", formattedDate);

                                HashMap receivermap = new HashMap<>();
                                receivermap.put("rid", receiverid);
                                receivermap.put("name", receiverName);
                                receivermap.put("pic", receiverPic);
                                receivermap.put("msg", "Send an image");
                                receivermap.put("status", "1");
                                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                                receivermap.put("date", formattedDate);

                                HashMap both_user_map = new HashMap<>();
                                both_user_map.put(inbox_sender_ref, receivermap);
                                both_user_map.put(inbox_receiver_ref, sendermap);

                                adduserToInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(getActivity()!=null){
                                            ChatActivity.sendPushNotification(getActivity(),  "Send an Image",
                                                    receiverid, senderid);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    // this method will upload the image in chhat
    public void sendGif(String url) {
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);


        DatabaseReference dref = rootref.child("chat").child(senderid + "-" + receiverid).push();
        final String key = dref.getKey();

        String current_user_ref = "chat" + "/" + senderid + "-" + receiverid;
        String chat_user_ref = "chat" + "/" + receiverid + "-" + senderid;

        HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverid);
        message_user_map.put("sender_id", senderid);
        message_user_map.put("chat_id", key);
        message_user_map.put("text", "");
        message_user_map.put("type", "gif");
        message_user_map.put("pic_url", url);
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", userName);
        message_user_map.put("timestamp", formattedDate);
        HashMap user_map = new HashMap<>();

        user_map.put(current_user_ref + "/" + key, message_user_map);
        user_map.put(chat_user_ref + "/" + key, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String inboxSenderRef = "Inbox" + "/" + senderid + "/" + receiverid;
                String inboxReceiverRef = "Inbox" + "/" + receiverid + "/" + senderid;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderid);
                sendermap.put("name", userName);
                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.uPic,""));
                sendermap.put("msg", "Send an gif image");
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverid);
                receivermap.put("name", receiverName);
                receivermap.put("pic", receiverPic);
                receivermap.put("msg", "Send an gif image");
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inboxSenderRef, receivermap);
                both_user_map.put(inboxReceiverRef, sendermap);

                adduserToInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(getActivity()!=null){
                            ChatActivity.sendPushNotification(getActivity(),
                                    "Send an gif image", receiverid, senderid);
                        }
                    }
                });
            }
        });
    }


    // this method will change the status to ensure that
    // user is seen all the message or not (in both chat node and Chatinbox node)
    public void changeStatus() {
        final Date c = Calendar.getInstance().getTime();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Query query1 = reference.child("chat").child(receiverid + "-" + senderid).orderByChild("status").equalTo("0");
        final Query query2 = reference.child("chat").child(senderid + "-" + receiverid).orderByChild("status").equalTo("0");

        final DatabaseReference inboxChangeStatus1 = reference.child("Inbox").child(senderid + "/" + receiverid);
        final DatabaseReference inboxChangeStatus2 = reference.child("Inbox").child(receiverid + "/" + senderid);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if (!nodeDataSnapshot.child("sender_id").getValue().equals(senderid)) {
                        String key = nodeDataSnapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time", Variables.df2.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if (!nodeDataSnapshot.child("sender_id").getValue().equals(senderid)) {
                        String key = nodeDataSnapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time", Variables.df2.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        inboxChangeStatus1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("rid").getValue().equals(receiverid)) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inboxChangeStatus1.updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inboxChangeStatus2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("rid").getValue().equals(receiverid)) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inboxChangeStatus2.updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void downloadAudio(final ProgressBar p_bar, ChatModel item) {
        p_bar.setVisibility(View.VISIBLE);
        PRDownloader.download(item.getPic_url(), direct.getPath(), item.getChat_id() + ".mp3")
                .build()
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        p_bar.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });

    }

    //this method will get the big size of image in private chat
    public void openAudio(String path) {
        PlayAudio_F playAudioF = new PlayAudio_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString("path", path);
        playAudioF.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Chat_F, playAudioF).commit();
    }


    // this is the delete message diloge which will show after long press in chat message
    private void deleteMessage(final ChatModel chat_model) {
        final CharSequence[] options = {context.getResources().getString(R.string.delete_this_message), context.getResources().getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle(null);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(context.getResources().getString(R.string.delete_this_message))) {
                    updateMessage(chat_model);
                } else if (options[item].equals(context.getResources().getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }


    // we will update the privious message means we will tells the other user that we have seen your message
    public void updateMessage(ChatModel item) {
        final String current_user_ref = "chat" + "/" + senderid + "-" + receiverid;
        final String chat_user_ref = "chat" + "/" + receiverid + "-" + senderid;


        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", item.getReceiver_id());
        message_user_map.put("sender_id", item.getSender_id());
        message_user_map.put("chat_id", item.getChat_id());
        message_user_map.put("text", "Delete this message");
        message_user_map.put("type", "delete");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", userName);
        message_user_map.put("timestamp", item.getTimestamp());

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + item.getChat_id(), message_user_map);
        user_map.put(chat_user_ref + "/" + item.getChat_id(), message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });

    }


    // this is the block dialog which will be show when user click on alert buttom of Top right in screen
    private void blockUserDialog() {
        final CharSequence[] options;
        if (isUserAlreadyBlock) {
            options = new CharSequence[]{getString(R.string.unmatch_this_user), getString(R.string.cancel)};
        }else {
            options = new CharSequence[]{getString(R.string.unmatch_this_user), getString(R.string.block_this_user), getString(R.string.cancel)};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle(null);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(getString(R.string.block_this_user))) {
                    openUserReport(receiverid);
                } else if (options[item].equals(getString(R.string.unblock_this_user))) {
                    //unBlockUser();
                }
                if (options[item].equals(getString(R.string.unmatch_this_user))) {
                    callApiDeleteInbox();
                } else if (options[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public void blockUser() {
        rootref.child("Inbox")
                .child(receiverid)
                .child(userId).child("block").setValue("1");
        rootref.child("Inbox").child(userId).child(receiverid).removeValue();
    }

    public void unBlockUser() {
        rootref.child("Inbox")
                .child(receiverid)
                .child(userId).child("block").setValue("0");
    }


    // we will delete only the today message so it is important to check the given message is the today message or not
    // if the given message is the today message then we will delete the message
    public boolean isTodayMessage(String date) {
        Calendar cal = Calendar.getInstance();
        int today_day = cal.get(Calendar.DAY_OF_MONTH);
        //current date in millisecond
        long currentTime = System.currentTimeMillis();

        //database date in millisecond
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        long databasedate = 0;
        Date d = null;
        try {
            d = f.parse(date);
            databasedate = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long difference = currentTime - databasedate;
        if (difference < 86400000) {
            int chatday = Integer.parseInt(date.substring(0, 2));
            if (today_day == chatday) {
                return true;
            }else{
                return false;
            }
        }

        return false;
    }


    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {
        final CharSequence[] options = { getActivity().getString(R.string.choose_from_Gallery), getActivity().getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(getActivity().getString(R.string.choose_from_Gallery))) {
                    callApiDeleteMatch();
                    if (checkReadStoragePermission()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        imageCallback.launch(intent);
                    }
                } else if (options[item].equals(getActivity().getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    ActivityResultLauncher<Intent> imageCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        InputStream imageStream = null;
                        try {
                            imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                        String path = getPath(selectedImage);
                        Matrix matrix = new Matrix();
                        ExifInterface exif = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            try {
                                exif = new ExifInterface(path);
                                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        matrix.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        matrix.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        matrix.postRotate(270);
                                        break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                        uploadImage(baos);
                    }
                }
            });


    // below tis the four types of permission
    //get the permission to record audio
    public boolean checkRecordPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    Variables.PERMISSION_RECORDING_AUDIO);
        }
        return false;
    }

    private boolean checkReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            try {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Variables.PERMISSION_READ_DATA);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }

    private boolean checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            try {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Variables.PERMISSION_WRITE_DATA);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Variables.PERMISSION_CAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getActivity().getString(R.string.tap_again), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, getActivity().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == Variables.PERMISSION_READ_DATA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getActivity().getString(R.string.tap_again), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == Variables.PERMISSION_WRITE_DATA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getActivity().getString(R.string.tap_again), Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == Variables.PERMISSION_RECORDING_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getActivity().getString(R.string.tap_again), Toast.LENGTH_SHORT).show();
            }
        }

    }


    public String getPath(Uri uri) {
        String result = null;
        String[] project = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, project, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(project[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }


    private void callApiForUseCoins(int usedCoins, String type) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("coin", ""+usedCoins);
            parameters.put("feature", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        ApiRequest.callApi(context, ApiLinks.useCoin, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);

                    String code = jsonObject.optString("code");
                    if(code.equals("200")){
                        JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");

                        Functions.getSharedPreference(context).edit()
                                .putInt(Variables.usedCoins, 0).apply();
                        Functions.getSharedPreference(context).edit()
                                .putString(Variables.uWallet, userObject.getString("wallet")).apply();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        callback.responce(new Bundle());
        super.onDestroyView();
    }


    // send the type indicator if the user is typing message
    public void sendTypingIndicator(boolean indicate) {
        // if the type indicator is present then we remove it if not then we create the typing indicator
        if (indicate) {
            final HashMap message_user_map = new HashMap<>();
            message_user_map.put("receiver_id", receiverid);
            message_user_map.put("sender_id", senderid);

            sendTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");
            sendTypingIndication.child(senderid + "-" + receiverid).setValue(message_user_map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sendTypingIndication.child(receiverid + "-" + senderid).setValue(message_user_map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
            });
        } else {
            sendTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");
            sendTypingIndication.child(senderid + "-" + receiverid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    sendTypingIndication.child(receiverid + "-" + senderid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            });
        }
    }


    // receive the type indication to show that your friend is typing or not
    LinearLayout mainLayout;
    public void receiveTypeIndication() {
        mainLayout = view.findViewById(R.id.typeindicator);

        receiveTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");
        receiveTypingIndication.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverid + "-" + senderid).exists()) {
                    String receiver = String.valueOf(dataSnapshot.child(receiverid + "-" + senderid).child("sender_id").getValue());
                    if (receiver.equals(receiverid)) {
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    mainLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //on destroy delete the typing indicator
    @Override
    public void onDestroy() {
        super.onDestroy();
        senderDdForCheckNotification = "";
        uploadingImageId = "none";
        sendTypingIndicator(false);
        queryGetChat.removeEventListener(eventListener);
        myBlockStatusQuery.removeEventListener(myInboxListener);
        otherBlockStatusQuery.removeEventListener(otherInboxListener);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        uploadingImageId = "none";
        senderDdForCheckNotification = "";
        sendTypingIndicator(false);
        queryGetChat.removeEventListener(eventListener);
        myBlockStatusQuery.removeEventListener(myInboxListener);
        otherBlockStatusQuery.removeEventListener(otherInboxListener);
    }


    //this method will get the big size of image in private chat
    public void openFullSizeImage(ChatModel item) {
        SeeFullImage_F see_image_f = new SeeFullImage_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", item.getPic_url());
        args.putSerializable("chat_id", item.getChat_id());
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Chat_F, see_image_f).commit();
    }


    // this is related with the list of Gifs that is show in the list below
    GifAdapter gifAdapter;
    final ArrayList<String> urlList = new ArrayList<>();
    RecyclerView gipsList;
    GPHApi client = new GPHApiClient(Constants.gifApiKey1);

    public void getGipy() {
        urlList.clear();
        gipsList = view.findViewById(R.id.gif_recylerview);
        gipsList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        gifAdapter = new GifAdapter(context, urlList, new GifAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                sendGif(item);
                slideDown();
            }
        });
        gipsList.setAdapter(gifAdapter);

        client.trending(MediaType.gif, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        for (Media gif : result.getData()) {
                            urlList.add(gif.getId());
                        }
                        gifAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }


    // if we want to search the gif then this mehtod is immportaant
    public void searchGif(String search) {
        /// Gif Search
        client.search(search, MediaType.gif, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        urlList.clear();
                        for (Media gif : result.getData()) {
                            urlList.add(gif.getId());
                            gifAdapter.notifyDataSetChanged();
                        }
                        gipsList.smoothScrollToPosition(0);
                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }


    // slide the view from below itself to the current position
    public void slideUp() {
        message.setHint("Search Gifs");
        uploadGifButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_gif_image));
        gifLayout.setVisibility(View.VISIBLE);
        sendButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_search));
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(700);
        animate.setFillAfter(true);
        gifLayout.startAnimation(animate);
    }


    // slide the view from its current position to below itself
    public void slideDown() {
        message.setHint("Type your message here...");
        message.setText("");
        uploadGifButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_gif_image_gray));
        sendButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_send));
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(700);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gifLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gifLayout.startAnimation(animate);
    }


    // this mehtos the will add a node of notification in to database
    // then our firebase cloud function will listen node and send the notification to spacific user
    public static void sendPushNotification(Activity context, String message,
                                            String receiverid, String senderid) {
        JSONObject notificationMap = new JSONObject();

        try {
            notificationMap.put("user_id", senderid);
            notificationMap.put("sender_id", senderid);
            notificationMap.put("receiver_id", receiverid);
            notificationMap.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.sendMessageNotification, notificationMap, null);

    }

    // open the view of Edit profile where 6 pic is visible
    public void profileDetail() {
        Intent intent = new Intent(getActivity(), ProfileDetails_A.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", receiverid);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }


    private boolean checkPermissionForCameraAndMicrophone() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int resultCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
            int resultMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
            return resultCamera == PackageManager.PERMISSION_GRANTED &&
                    resultMic == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermissionForCameraAndMicrophone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    shouldShowRequestPermissionRationale(
                            Manifest.permission.RECORD_AUDIO)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                        Variables.CAMERA_MIC_PERMISSION_REQUEST_CODE);
            }
        }
    }


}
