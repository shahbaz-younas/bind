package com.qboxus.binder.MainMenu;


import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.qboxus.binder.ActivitiesFragments.Chat.ChatActivity;
import com.qboxus.binder.ActivitiesFragments.Inbox.Inbox_F;
import com.qboxus.binder.ActivitiesFragments.UserLikes.Userlikes_F;
import com.qboxus.binder.Models.InviteFriendModel;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Interfaces.FragmentCallback;
import com.qboxus.binderstatic.RelateToFragment_OnBack.OnBackPressListener;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binder.ActivitiesFragments.Profile.Profile_F;
import com.qboxus.binder.R;
import com.qboxus.binder.ActivitiesFragments.Users.Users_F;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainMenuFragment extends RootFragment implements View.OnClickListener {

    protected CustomViewPager pager;

    private ViewPagerAdapter adapter;
    Context context;

    String userId;
    ImageButton profileBtn, starBtn, binderBtn, messageBtn;


    public MainMenuFragment() {
        //Required Empty
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        context=getContext();
        userId = Functions.getSharedPreference(context).getString(Variables.uid,"");

        pager = view.findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(3);
        pager.setPagingEnabled(false);
        view.setOnClickListener(this);


        profileBtn =view.findViewById(R.id.profile_btn);
        profileBtn.setOnClickListener(this);

        binderBtn =view.findViewById(R.id.binder_btn);
        binderBtn.setOnClickListener(this);

        starBtn =view.findViewById(R.id.star_btn);
        starBtn.setOnClickListener(this);

        messageBtn =view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.profile_btn:
                clickProfile();
                pager.setCurrentItem(3);
                break;

            case R.id.binder_btn:
                pager.setCurrentItem(0);
                clickBinder();
                break;

            case R.id.star_btn:
                pager.setCurrentItem(1);
                clickStar();
                break;

            case R.id.message_btn:
                clickMessage();
                pager.setCurrentItem(2);
                break;
        }
    }


    public void clickProfile(){

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_color));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickBinder(){

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_color));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickStar(){

        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_color));
        profileBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickMessage(){

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_color));

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        setupTabIcons();
    }


    public boolean onBackPressed() {
        // currently visible tab Fragment
        OnBackPressListener currentFragment = (OnBackPressListener) adapter.getRegisteredFragment(pager.getCurrentItem());

        if (currentFragment != null) {
            // lets see if the currentFragment or any of its childFragment can handle onBackPressed
            return currentFragment.onBackPressed();
        }

        // this Fragment couldn't handle the onBackPressed call
        return false;
    }


    private void setupTabIcons() {
        adapter.addFrag(new Users_F());
        adapter.addFrag(new Userlikes_F());
        adapter.addFrag(new Inbox_F());
        adapter.addFrag(new Profile_F());

        pager.setCurrentItem(0);
        adapter.notifyDataSetChanged();

        Log.d(Constants.tag, "setupTabIcons");
        Log.d(Constants.tag, "MainMenuActivity.actionType:"+MainMenuActivity.actionType);

        if(MainMenuActivity.actionType.equals("message")){
            pager.setCurrentItem(2);
            chatFragment();
        }else if(MainMenuActivity.actionType.equals("match")){
            pager.setCurrentItem(2);
        }else {
            pager.setCurrentItem(0);
        }

    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        public void replaceFrag(int pos,Fragment fragment) {
            registeredFragments.remove(pos);
            mFragmentList.remove(pos);
            mFragmentList.add(pos,fragment);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }


    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    public void chatFragment(){
        ChatActivity chatActivity = new ChatActivity(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {

            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle args = new Bundle();
        args.putString("Sender_Id", userId);
        args.putString("Receiver_Id",MainMenuActivity.receiverid);
        args.putString("name",MainMenuActivity.title);
        args.putString("picture",MainMenuActivity.receiverPic);
        args.putBoolean("is_match_exits",false);
        chatActivity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, chatActivity).commit();
    }


}