package com.qboxus.binder_pro.ActivitiesFragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Interfaces.AdapterClickListener;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.Models.FreeVideoCreditModel;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.GridSpacingItemDecoration;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.qboxus.R;
import com.qboxus.binder_pro.Adapters.FreeVideoCreditsAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class FreeCreditsVideo_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    RecyclerView rv;
    FreeVideoCreditsAdapter adapter;

    FreeVideoCreditModel model = new FreeVideoCreditModel();
    int position = 0;

    ImageButton backButton;
    TextView continueButton;
    public static String selectedId = "";


    public FreeCreditsVideo_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_free_credits_video, container, false);
        context=getContext();

        initializeViews();

        return view;
    }


    private void initializeViews() {
        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);
        rv = view.findViewById(R.id.rv);

        setAdapterToRecyclerView();
    }


    private void setAdapterToRecyclerView() {
        GridLayoutManager layout = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layout);
        rv.setHasFixedSize(false);

        adapter = new FreeVideoCreditsAdapter(getContext(), Constants.freeCreditsVideosList(), new AdapterClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(int pos, Object object, View view) {
                model = (FreeVideoCreditModel) object;
                position = pos;
                Constants.freeCreditsVideosList().remove(position);
                selectedId = model.getPackageId();
                Constants.freeCreditsVideosList().add(position, model);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {
            }
        });

        rv.setAdapter(adapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen._10sdp);
        rv.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
    }

    public void showRewardedAds(){
        if(mRewardedAd != null){
            mRewardedAd.show(getActivity(), new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                }
            });

            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    int viewNumberOfVideos = Functions.getSharedPreference(context)
                            .getInt(model.getPackageId()+Functions
                                    .getSharedPreference(context).getString(Variables.uid,""),0);
                    Functions.getSharedPreference(context)
                            .edit().putInt(model.getPackageId()+Functions
                            .getSharedPreference(context).getString(Variables.uid,""), viewNumberOfVideos+1).commit();

                    int viewNumberOfVideos1 = Functions.getSharedPreference(context)
                            .getInt(model.getPackageId()+Functions
                                    .getSharedPreference(context).getString(Variables.uid,""),0);
                    int numberOfVideos = Integer.parseInt(model.getNoOfVideos());

                    if(viewNumberOfVideos1 < numberOfVideos){
                        Functions.getSharedPreference(context)
                                .edit().putInt(model.getPackageId()+Functions
                                .getSharedPreference(context).getString(Variables.uid,""), viewNumberOfVideos1).commit();
                    }else {
                        Functions.getSharedPreference(context)
                                .edit().putInt(model.getPackageId()+Functions
                                .getSharedPreference(context).getString(Variables.uid,""), 0).commit();
                        callApiPurchaseCoins();
                    }

                    adapter.notifyItemChanged(position);
                }
            });
        }
    }


    RewardedAd mRewardedAd;
    @Override
    public void onResume() {
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(getContext(),
                context.getString(R.string.my_Rewarded_Add),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }
                });
    }


    private void callApiPurchaseCoins() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid,""));
            parameters.put("title", model.getCredits()+" coins");
            parameters.put("coin", model.getCredits());
            parameters.put("price", "");
            parameters.put("purchase_id", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        ApiRequest.callApi(context, ApiLinks.purchaseCoin, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");

                    if(code.equals("200")){
                        JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");

                        Functions.getSharedPreference(context).edit()
                                .putString(Variables.uWallet, userObject.optString("wallet")).commit();

                        selectedId = "";
                        getParentFragmentManager().setFragmentResult("1222", new Bundle());
                        getActivity().onBackPressed();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backButton) {
            selectedId = "";
            getParentFragmentManager().setFragmentResult("1222", new Bundle());
            getActivity().onBackPressed();
        } else if (id == R.id.continueButton) {
            if (model.getNoOfVideos() != null) {
                showRewardedAds();
            }
        }
    }

}