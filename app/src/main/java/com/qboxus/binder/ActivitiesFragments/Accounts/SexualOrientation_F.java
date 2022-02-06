package com.qboxus.binder.ActivitiesFragments.Accounts;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.binder.Adapters.SexualOrientationAdapter;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Interfaces.AdapterClickListener;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binder.Models.SexualOrientationModel;
import com.qboxus.binder.R;
import com.qboxus.binderstatic.SimpleClasses.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SexualOrientation_F extends Fragment {

    RecyclerView sexualOrientation;
    SexualOrientationAdapter adapter;
    ArrayList<SexualOrientationModel> list = new ArrayList<>();

    ImageView checkbox;
    LinearLayout check;
    Boolean isCheck = false;
    RelativeLayout continueSexual;
    TextView continueTv;

    int selectedItem = 0;

    View view;

    public SexualOrientation_F() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sexual_orientation, container, false);

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
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
                Signup_A.userModel.orientationList.clear();
                Signup_A.userModel.show_orientation = "0";
                Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()+1);
                Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                        Signup_A.pager.getCurrentItem() + 1,
                        Signup_A.pager.getOffscreenPageLimit()));
            }
        });


        sexualOrientation = view.findViewById(R.id.sexual_orien_rv);
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        sexualOrientation.setLayoutManager(layout);
        sexualOrientation.setHasFixedSize(false);
        adapter =new SexualOrientationAdapter(getContext(), list, new AdapterClickListener() {
            @Override
            public void onItemClick(int pos, Object object, View view) {
                SexualOrientationModel model = (SexualOrientationModel) object;

                if(Signup_A.userModel.orientationList.size() == 0){
                    Signup_A.userModel.orientationList.add(model);
                    selectedItem = selectedItem + 1;
                    list.remove(model);
                    if(model.getCheck()){
                        selectedItem = selectedItem - 1;
                        model.setCheck(false);
                    }else if(!model.getCheck()){
                        model.setCheck(true);
                    }
                    list.add(pos, model);

                    adapter.notifyItemChanged(pos);
                }else if(Signup_A.userModel.orientationList.size()>0){
                    for(int i = 0; i<Signup_A.userModel.orientationList.size(); i++){
                        SexualOrientationModel model1 = Signup_A.userModel.orientationList.get(i);
                        if(model1.getSexualOrientation().equals(model.getSexualOrientation())){
                            Signup_A.userModel.orientationList.remove(i);
                            list.remove(model);
                            if(model.getCheck()){
                                selectedItem = selectedItem - 1;
                                model.setCheck(false);
                            }else if(!model.getCheck()){
                                selectedItem = selectedItem + 1;
                                model.setCheck(true);
                            }
                            list.add(pos, model);

                            adapter.notifyItemChanged(pos);
                            break;
                        }else if(i+1 == Signup_A.userModel.orientationList.size() && !model1.getSexualOrientation().equals(model.getSexualOrientation())){
                            if(Signup_A.userModel.orientationList.size() < 3){
                                Signup_A.userModel.orientationList.add(model);
                                list.remove(model);
                                if(model.getCheck()){
                                    selectedItem = selectedItem - 1;
                                    model.setCheck(false);
                                }else if(!model.getCheck()){
                                    selectedItem = selectedItem + 1;
                                    model.setCheck(true);
                                }
                                list.add(pos, model);

                                adapter.notifyItemChanged(pos);
                                continueSexual.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_google_background));
                                continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                                break;
                            }
                        }
                    }
                }

                if(Signup_A.userModel.orientationList.size() > 0){
                    continueSexual.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pink_background));
                    continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                }else if(Signup_A.userModel.orientationList.size() < 1){
                    continueSexual.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_google_background));
                    continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                }

            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {
            }
        });
        sexualOrientation.setAdapter(adapter);
        callApiShowSexualOrientation();

        checkbox = view.findViewById(R.id.checkbox);
        check = view.findViewById(R.id.checkLayout);

        if(isCheck){
            Signup_A.userModel.show_orientation = "1";
            checkbox.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_fill));
            isCheck = false;
        }else if(!isCheck){
            Signup_A.userModel.show_orientation = "0";
            checkbox.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_empty));
            isCheck = true;
        }

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCheck){
                    Signup_A.userModel.show_orientation = "1";
                    checkbox.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_fill));
                    isCheck = false;
                }else {
                    Signup_A.userModel.show_orientation = "0";
                    checkbox.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_empty));
                    isCheck = true;
                }
            }
        });

        continueSexual = view.findViewById(R.id.continueButton);
        continueTv = view.findViewById(R.id.continue_tv);
        continueSexual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Signup_A.userModel.orientationList.size() > 0){
                    Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()+1);
                    Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                            Signup_A.pager.getCurrentItem() + 1,
                            Signup_A.pager.getOffscreenPageLimit()));
                }
            }
        });

        return  view;
    }

    private void callApiShowSexualOrientation() {
        ApiRequest.callApi(getActivity(), ApiLinks.showSexualOrientation, new JSONObject(), new Callback() {
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
                list.clear();
                JSONArray msgArray = jsonObject.optJSONArray("msg");
                for (int i=0; i<msgArray.length();i++){
                    SexualOrientationModel model = new SexualOrientationModel();
                    model.setId(msgArray.getJSONObject(i).optJSONObject("SexualOrientation").getString("id"));
                    model.setSexualOrientation(msgArray.getJSONObject(i).optJSONObject("SexualOrientation").getString("title"));
                    model.setCheck(false);

                    list.add(model);
                }
                adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}