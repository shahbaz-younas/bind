package com.qboxus.binder.ActivitiesFragments.Accounts;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binder.Models.PassionsModel;
import com.qboxus.binder.R;
import com.qboxus.binderstatic.SimpleClasses.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class Passions extends Fragment {

    List<PassionsModel> list = new ArrayList<>();
    List<String> tempList = new ArrayList<>();

    ChipGroup chipGroup;

    RelativeLayout continueButton;
    TextView continueTv;

    SharedPreferences prefs;
    View view;

    public Passions() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_passions, container, false);

        prefs = getActivity().getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                        Signup_A.pager.getCurrentItem(),
                        Signup_A.pager.getOffscreenPageLimit()));
                Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()-1);
            }
        });

        view.findViewById(R.id.skip_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Signup_A.userModel.userPassion.clear();
                Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()+1);
                Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                        Signup_A.pager.getCurrentItem() + 1,
                        Signup_A.pager.getOffscreenPageLimit()));
            }
        });

        continueButton = view.findViewById(R.id.continueButton);
        continueTv = view.findViewById(R.id.continue_tv);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempList.size() > 2){
                    Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()+1);
                    Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                            Signup_A.pager.getCurrentItem() + 1,
                            Signup_A.pager.getOffscreenPageLimit()));
                }
            }
        });

        chipGroup = view.findViewById(R.id.chipGroup);

        callApiShowPassions();

        return view;
    }

    private void callApiShowPassions() {
        ApiRequest.callApi(getActivity(), ApiLinks.showPassions, new JSONObject(), new Callback() {
            @Override
            public void response(String resp) {
                parseUserInfo(resp);
            }
        });
    }

    public void parseUserInfo(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                prefs.edit().putString(Variables.uPassions, jsonObject.toString()).apply();
                list.clear();
                JSONArray msgArray = jsonObject.optJSONArray("msg");

                for (int i=0; i<msgArray.length();i++){
                    PassionsModel model = new PassionsModel();

                    model.setId(msgArray.getJSONObject(i).optJSONObject("Passion").getString("id"));
                    model.setTitle(msgArray.getJSONObject(i).optJSONObject("Passion").getString("title"));

                    list.add(model);
                    Chip chip1 = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_passion, null);
                    chip1.setText(msgArray.getJSONObject(i).optJSONObject("Passion").getString("title"));
                    chip1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(tempList.size() == 0){
                                tempList.add(chip1.getText().toString());
                                Signup_A.userModel.userPassion.add(model.getId());
                                chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.pink_color)));
                                chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.pink_color)));
                                continueTv.setText(getContext().getString(R.string.continue_capital)+" ("+tempList.size()+"/5)");
                            }else if(tempList.size()>0){
                                for(int i = 0; i<tempList.size(); i++){
                                    if(tempList.get(i).equals(chip1.getText().toString())){
                                        chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.newGrayTextColor)));
                                        chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.newGrayTextColor)));
                                        tempList.remove(i);
                                        continueTv.setText(getContext().getString(R.string.continue_capital)+" ("+tempList.size()+"/5)");
                                        Signup_A.userModel.userPassion.remove(model.getId());
                                        break;
                                    }else if(i+1 == tempList.size() && !tempList.get(i).equals(chip1.getText().toString())){
                                        if(tempList.size() < 5){
                                            tempList.add(chip1.getText().toString());
                                            Signup_A.userModel.userPassion.add(model.getId());
                                            chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.pink_color)));
                                            chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.pink_color)));
                                            continueTv.setText(getContext().getString(R.string.continue_capital)+" ("+tempList.size()+"/5)");
                                            break;
                                        }
                                    }
                                }
                            }
                            if(tempList.size() > 2){
                                continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pink_background));
                                continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                            }else {
                                continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_google_background));
                                continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                            }
                        }
                    });

                    chipGroup.addView(chip1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}