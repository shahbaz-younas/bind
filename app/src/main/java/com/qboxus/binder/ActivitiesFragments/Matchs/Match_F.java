package com.qboxus.binder.ActivitiesFragments.Matchs;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qboxus.binderstatic.Models.MatchModel;
import com.qboxus.binder.ActivitiesFragments.Chat.ChatActivity;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.FragmentCallback;
import com.qboxus.binder.MainMenu.MainMenuActivity;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binder.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labo.kaji.fragmentanimations.MoveAnimation;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */


// this is the view which is show when both users like each other

public class Match_F extends RootFragment {

    View view;
    Context context;

    TextView matchTxt;
    ImageView user1Pic, user2Pic, superLike;

    LinearLayout sendMessageLayout;

    MatchModel withoutPurchaseItem;

    DatabaseReference rootref;
    SharedPreferences prefs;

    public Match_F() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view= inflater.inflate(R.layout.fragment_match, container, false);
        context=getContext();
        prefs = getActivity().getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        ImageButton cross_btn=view.findViewById(R.id.cross_btn);
        cross_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        rootref=FirebaseDatabase.getInstance().getReference();

        matchTxt =view.findViewById(R.id.match_txt);
        user1Pic =view.findViewById(R.id.user1_pic);
        user2Pic =view.findViewById(R.id.user2_pic);
        superLike =view.findViewById(R.id.superLike);

        sendMessageLayout =view.findViewById(R.id.send_message_layout);

        Bundle bundle=getArguments();
        if(bundle!=null){
            // get user data from previous view and show in that view
            withoutPurchaseItem = (MatchModel) bundle.getSerializable("data");
            if(withoutPurchaseItem.getSuperLike().equals("1")){
                superLike.setVisibility(View.VISIBLE);
            }else if(withoutPurchaseItem.getSuperLike().equals("0")){
                superLike.setVisibility(View.GONE);
            }

            matchTxt.setText(context.getString(R.string.you_and)+" "+
                    withoutPurchaseItem.getUsername()+" "+context.getString(R.string.like_each_other));

            if(!prefs.getString(Variables.uPic, "").equals("")){
                Uri uri = Uri.parse(Constants.BASE_URL+prefs.getString(Variables.uPic, ""));
                user1Pic.setImageURI(uri);
            }

            if(withoutPurchaseItem.getPicture()!=null && !withoutPurchaseItem.getPicture().equals("")){
                Uri uri = Uri.parse(Constants.BASE_URL+ withoutPurchaseItem.getPicture());
                user2Pic.setImageURI(uri);
            }

        }

        // click listener of message btn
        sendMessageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFragment(Functions.getSharedPreference(context).getString(Variables.uid,""), withoutPurchaseItem.getU_id(), withoutPurchaseItem.getUsername(), withoutPurchaseItem.getPicture());
            }
        });


        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            Animation anim= MoveAnimation.create(MoveAnimation.UP, enter, 300);
            return anim;

        } else {
            return MoveAnimation.create(MoveAnimation.DOWN, enter, 300);
        }
    }


    //open the chat fragment and on withoutPurchaseItem click and pass your id and the other person id in which
    //you want to chat with them
    public void chatFragment(String senderid,String receiverid,String name,String picture){
        getActivity().onBackPressed();
        ChatActivity chat_activity = new ChatActivity(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {

            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("Sender_Id",senderid);
        args.putString("Receiver_Id",receiverid);
        args.putString("picture",picture);
        args.putString("name",name);
        args.putBoolean("is_match_exits",true);
        chat_activity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
    }


    // when this screen open it will send the notification to other user that
    // both are like the each other and match will build between the users
    public void sendPushNotification(final String receverid){
        rootref.child("Users").child(receverid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                String token=dataSnapshot.child("token").getValue().toString();
                JSONObject notimap=new JSONObject();
                    try {
                        notimap.put("title",Functions.getSharedPreference(context).getString(Variables.fName,""));
                        notimap.put("message","Congrats! you got a match");

                        String image=MainMenuActivity.userPic;
                        if(!image.contains("http")){
                            image= Constants.BASE_URL+MainMenuActivity.userPic;
                        }
                        notimap.put("icon", image);
                        notimap.put("tokon",token);
                        notimap.put("senderid",Functions.getSharedPreference(context).getString(Variables.uid,""));
                        notimap.put("receiverid", receverid);
                        notimap.put("action_type", "match");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ApiRequest.callApi(context, ApiLinks.sendMessageNotification, notimap,null);


            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
