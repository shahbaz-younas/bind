package com.qboxus.binder.ActivitiesFragments.Accounts;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.qboxus.binder.MainMenu.CustomViewPager;
import com.qboxus.binder.Models.UserModel;
import com.qboxus.binder.R;
import com.qboxus.binderstatic.SimpleClasses.Functions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Signup_A extends AppCompatActivity {


    public static CustomViewPager pager;
    public static ViewPagerAdapter adapter;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;

    public static UserModel userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        Bundle bundle = getIntent().getBundleExtra("bundle");
        if(bundle!=null){
            userModel = (UserModel) bundle.getSerializable("user_model");
        }

        progressBar = findViewById(R.id.pb);

        pager = findViewById(R.id.vp);
        if(!userModel.isFromPh && userModel.isSocialLogin){
            pager.setOffscreenPageLimit(10);
        }else if(!userModel.isFromPh){
            pager.setOffscreenPageLimit(11);
        }else {
            pager.setOffscreenPageLimit(8);
        }
        pager.setPagingEnabled(false);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        if(!userModel.isFromPh){
            adapter.addFrag(new Phone_F());
            adapter.addFrag(new Phone_Otp_F(false));
        }
        adapter.addFrag(new FirstName_F(""));
        if(!userModel.isFromPh && !userModel.isSocialLogin){
            adapter.addFrag(new Password_F());
        }
        adapter.addFrag(new DOB_F());
        adapter.addFrag(new Gender_F(true));
        adapter.addFrag(new SexualOrientation_F());
        adapter.addFrag(new Gender_F(false));
        adapter.addFrag(new MySchool_F());
        adapter.addFrag(new Passions());
        adapter.addFrag(new AddPhotos());

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        switch (pager.getCurrentItem()){
            case 0:
                super.onBackPressed();
                break;
            case 1:
                pager.setCurrentItem(0);
                progressBar.setProgress(13);
                break;
            case 2:
                calculateProgress();
                break;
            case 3:
                calculateProgress();
                break;
            case 4:
                calculateProgress();
                break;
            case 5:
                calculateProgress();
                break;
            case 6:
                calculateProgress();
                break;
            case 7:
                calculateProgress();
                break;
            case 8:
                calculateProgress();
                break;
            case 9:
                calculateProgress();
                break;
            case 10:
                calculateProgress();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        @SuppressLint("WrongConstant")
        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, 0);
        }

        @NotNull
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

        @NotNull
        @Override
        public Object instantiateItem(@NotNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    public static void calculateProgress(){
        if(Signup_A.userModel.isFromPh){
            Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                    Signup_A.pager.getCurrentItem(),
                    8));
        }else {
            Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                    Signup_A.pager.getCurrentItem(),
                    11));
        }
        pager.setCurrentItem(pager.getCurrentItem()-1);
    }
}