package com.qboxus.binder_pro.ActivitiesFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.qboxus.R;
import com.qboxus.binder_pro.InAppSubscription.InAppSubscription_A;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Boost_F extends RootFragment implements View.OnClickListener , PurchasesUpdatedListener {

    View view;
    Context context;
    CircularProgressBar circularProgressBar;

    RelativeLayout rl1,rl2,rl3,boostButton,seperaterView;
    LinearLayout ll1,goldView, walletView;
    TextView tv1,tv2,boostDescTV;

    String totalBoost = "";
    String boostNum = "";
    String boostId = "";
    String userId;

    int position = 2, wallet;

    List<SkuDetails> storedList = new ArrayList<>();
    public BillingClient billingClient;

    public Boost_F() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();

        if (!checkIsBoostOn()) {
            view = inflater.inflate(R.layout.fragment_boost, container, false);
            view.findViewById(R.id.boost_btn).setOnClickListener(this);
        } else {
            view = inflater.inflate(R.layout.fragment_boost_on, container, false);
            circularProgressBar = view.findViewById(R.id.circularProgressBar);
            view.findViewById(R.id.okay_btn).setOnClickListener(this);
            setProgress();
        }

        userId = Functions.getSharedPreference(context).getString(Variables.uid, "");
        view.findViewById(R.id.transparent_layout).setOnClickListener(this);

        totalBoost = Functions.getSharedPreference(context).getString(Variables.uTotalBoost, "");
        wallet = Integer.parseInt(Functions.getSharedPreference(context).getString(Variables.uWallet, "0"));

        if(!checkIsBoostOn()){
            initializeViews();
        }

        initializeBill();

        return view;
    }


    public void initializeBill(){
        billingClient=BillingClient.newBuilder(getActivity())
                .enablePendingPurchases()
                .setListener(this::onPurchasesUpdated)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //loadAllProducts();
                    InitPurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(Constants.tag,"onBillingServiceDisconnected");
            }
        });

    }


    @Override
    public void onDetach() {
        billingClient.endConnection();
        super.onDetach();
    }


    private void InitPurchases() {
        final List<String> skuList=new ArrayList<>();
        skuList.add(Constants.boostID);

        SkuDetailsParams.Builder params=SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);

        params.setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build()
                , (billingResult, list) -> {
                    storedList.clear();
                    storedList.addAll(list);
                    Log.d(Constants.tag,"Billing : onSkuDetailsResponse ");
                });

    }


    public void purchaseSubscription(int postion){
        if (billingClient.isReady()) {
            switch (postion) {
                case 1: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.boostID)) {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(getActivity(), flowParams);
                        }
                    }
                }
                break;
                case 2: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.boostID2)) {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(getActivity(), flowParams);
                        }
                    }
                }
                break;
                case 3: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.boostID3)) {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(getActivity(), flowParams);
                        }
                    }
                }
                break;
            }
        }
        else {
            Log.d(Constants.tag,"Billing : purchaseSubscription ");
        }
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            handlePurchases(list);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(Constants.tag,""+billingResult.getResponseCode()+"--"+BillingClient.BillingResponseCode.USER_CANCELED);
        } else {
            Log.d(Constants.tag,""+billingResult.getResponseCode());
        }
    }


    private void handlePurchases(List<Purchase> purchases) {
        for (Purchase skuList:purchases) {
            if (Constants.boostID.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED) {
                if (!skuList.isAcknowledged()) {
                    Log.d(Constants.tag,"!skuList.isAcknowledged()");
                    AcknowledgePurchaseParams acknowledgeParams=AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(skuList.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgeParams, billingResult -> {
                        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK ) {
                            callApiBoostProfilePurchase();
                        }
                    });
                }
            }
        }
    }


    private void initializeViews() {
        ll1 = view.findViewById(R.id.ll1);

        rl1 = view.findViewById(R.id.rl1);
        rl2 = view.findViewById(R.id.rl2);
        rl3 = view.findViewById(R.id.rl3);

        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);

        tv1 = view.findViewById(R.id.tv1);
        tv2 = view.findViewById(R.id.tv2);

        rl1.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
        rl2.setBackground(ContextCompat.getDrawable(context, R.drawable.d_blue_border));
        rl3.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
        tv1.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.GONE);

        boostId = Constants.boostID2;
        boostNum = Constants.boostNumber2;

        walletView = view.findViewById(R.id.walletView);
        walletView.setOnClickListener(this);
        goldView = view.findViewById(R.id.goldView);
        goldView.setOnClickListener(this);

        boostButton = view.findViewById(R.id.boost_btn);
        seperaterView = view.findViewById(R.id.seperaterView);
        boostDescTV = view.findViewById(R.id.boostDescTV);
        boostDescTV.setText(context.getString(R.string._1_boost_with_)+" "+Constants.BOOST_COINS+" "+context.getString(R.string.coins_));

        int totalBoost = Integer.parseInt(Functions.getSharedPreference(context)
                .getString(Variables.uTotalBoost,"0"));


        if(totalBoost>0){
            ll1.setVisibility(View.GONE);
            boostButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_pink_background));
            seperaterView.setVisibility(View.GONE);
            goldView.setVisibility(View.GONE);
        }else {
            ll1.setVisibility(View.VISIBLE);
            boostButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_blue_btn));
            seperaterView.setVisibility(View.VISIBLE);
            goldView.setVisibility(View.VISIBLE);
        }
    }


    long timeGone;
    public boolean checkIsBoostOn() {
        long requestTime = Long.parseLong( Functions.getSharedPreference(context).getString(Variables.boostOnTime, "0"));
        long currentTime = System.currentTimeMillis();

        timeGone = (currentTime - requestTime);

        if(Functions.getSharedPreference(context).getString(Variables.uBoost,"0").equals("0")){
            return false;
        }else {
            return true;
        }

    }


    public void setProgress() {
        long requestTime = Long.parseLong( Functions.getSharedPreference(context).getString(Variables.boostOnTime, "0"));
        long currentTime = System.currentTimeMillis();

        timeGone = (currentTime - requestTime);
        startTimer();
    }


    CountDownTimer timer;
    public void startTimer() {
        long timeLeft = Constants.BOOST_TIME_DURATION - timeGone;
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long l) {
                long millis = l;

                String timeString = Functions.convertSeconds((int) (millis / 1000));
                TextView textView = view.findViewById(R.id.remaining_txt);
                textView.setText(timeString+" "+context.getString(R.string.Remaining));

                float progress = ((l * 100) / Constants.BOOST_TIME_DURATION);
                circularProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                stopTimer();
            }
        };

        timer.start();
    }

    public void stopTimer() {
        if (timer != null){
            timer.cancel();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.boost_btn) {
            int totalBoost = Integer.parseInt(Functions.getSharedPreference(context)
                    .getString(Variables.uTotalBoost, "0"));
            if (totalBoost > 0) {
                callApiForBoostProfile();
            } else {
                purchaseSubscription(position);
            }
        } else if (id == R.id.transparent_layout) {
            getActivity().onBackPressed();
        } else if (id == R.id.okay_btn) {
            getActivity().onBackPressed();
        } else if (id == R.id.rl1) {
            rl1.setBackground(ContextCompat.getDrawable(context, R.drawable.d_blue_border));
            rl2.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            rl3.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            position = 1;
            boostId = Constants.boostID;
            boostNum = Constants.boostNumber;
        } else if (id == R.id.rl2) {
            rl1.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            rl2.setBackground(ContextCompat.getDrawable(context, R.drawable.d_blue_border));
            rl3.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            tv1.setVisibility(View.VISIBLE);
            tv2.setVisibility(View.GONE);
            position = 2;
            boostId = Constants.boostID2;
            boostNum = Constants.boostNumber2;
        } else if (id == R.id.rl3) {
            rl1.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            rl2.setBackground(ContextCompat.getDrawable(context, R.drawable.d_white_border));
            rl3.setBackground(ContextCompat.getDrawable(context, R.drawable.d_blue_border));
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.VISIBLE);
            position = 3;
            boostId = Constants.boostID3;
            boostNum = Constants.boostNumber3;
        } else if (id == R.id.walletView) {
            if (wallet > Constants.BOOST_COINS || wallet == Constants.BOOST_COINS) {
                callApiForUseCoins();
            } else {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                Purchase_coins_F fragment = new Purchase_coins_F();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.MainMenuFragment, fragment).commit();
            }
        } else if (id == R.id.goldView) {
            openSubscriptionView();
        }
    }


    // when user will click the refresh btn  then this view will be open for subscribe it in our app
    public void openSubscriptionView(){
        if(!Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase,false)){
            startActivity(new Intent(getActivity(), InAppSubscription_A.class));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }


    private void callApiForUseCoins() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("coin", ""+Constants.BOOST_COINS);
            parameters.put("feature", "boost");
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
                                .putString(Variables.uWallet, userObject.getString("wallet")).apply();

                        callApiForBoostProfile();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void callApiForBoostProfile() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        ApiRequest.callApi(context, ApiLinks.boostProfile, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);

                    String code = jsonObject.optString("code");
                    if(code.equals("200")){
                        JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");

                        Functions.getSharedPreference(context).edit()
                                .putString(Variables.uTotalBoost, userObject.getString("total_boost")).commit();

                        Functions.getSharedPreference(context).edit()
                                .putString(Variables.uBoost, userObject.getString("boost")).commit();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                long min = System.currentTimeMillis();
                Functions.getSharedPreference(context).edit().putString(Variables.boostOnTime, "" + min).commit();
                getActivity().onBackPressed();
            }
        });
    }

    private void callApiBoostProfilePurchase() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("transaction_id", boostId);
            parameters.put("no_of_boost", boostNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Functions.showLoader(getActivity(), false, false);
                ApiRequest.callApi(context, ApiLinks.boostPurchase, parameters, new Callback() {
                    @Override
                    public void response(String resp) {
                        Functions.cancelLoader();

                        try {
                            JSONObject jsonObject = new JSONObject(resp);

                            String code = jsonObject.optString("code");
                            if(code.equals("200")){
                                JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");

                                Functions.getSharedPreference(context).edit()
                                        .putString(Variables.uTotalBoost, userObject.getString("total_boost")).commit();

                                Functions.getSharedPreference(context).edit()
                                        .putString(Variables.uBoost, userObject.getString("boost")).commit();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        long min = System.currentTimeMillis();
                        Functions.getSharedPreference(context).edit().putString(Variables.boostOnTime, "" + min).commit();
                        getActivity().onBackPressed();
                    }
                });
            }
        });

    }

}
