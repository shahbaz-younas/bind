package com.qboxus.binder_pro.InAppSubscription;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

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
import com.qboxus.binderstatic.Models.PurchaseModel;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.google.android.material.tabs.TabLayout;
import com.qboxus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;


public class InAppSubscription_A extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener {

    SharedPreferences sharedPreferences;
    RelativeLayout purchaseBtn;

    TextView goBack;

    LinearLayout subLayout1, subLayout2, subLayout3;

    List<SkuDetails> storedList = new ArrayList<>();
    public BillingClient billingClient;

    String duration,amount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_subscription);

        // get the shared preference
        sharedPreferences=getSharedPreferences(Variables.prefName,MODE_PRIVATE);

        purchaseBtn =findViewById(R.id.purchase_btn);
        purchaseBtn.setOnClickListener(this);


        goBack =findViewById(R.id.Goback);
        goBack.setOnClickListener(this);

        setSlider();

        subLayout1 = findViewById(R.id.sub_layout1);
        subLayout2 =findViewById(R.id.sub_layout2);
        subLayout3 = findViewById(R.id.sub_layout3);

        selectOne(1);
        amount = Constants.productIdamount2;
        duration = Constants.productIdDuration2;

        subLayout1.setOnClickListener(this);
        subLayout2.setOnClickListener(this);
        subLayout3.setOnClickListener(this);

        initializeBill();
    }


    public void initializeBill(){
        billingClient=BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this::onPurchasesUpdated)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    InitPurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(Constants.tag,"onBillingServiceDisconnected");
            }
        });
    }


    private void InitPurchases() {
        final List<String> skuList=new ArrayList<>();
        skuList.add(Constants.productID);

        SkuDetailsParams.Builder params=SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);

        params.setType(SUBS);
        billingClient.querySkuDetailsAsync(params.build()
                , (billingResult, list) -> {
                    storedList.clear();
                    storedList.addAll(list);
                });
    }


    public void purchaseSubscription(int postion){
        if (billingClient.isReady()) {
            switch (postion) {
                case 0: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.productID)) {
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(this, flowParams);
                        }
                    }
                }
                break;
                case 1: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.productID2)) {
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(this, flowParams);
                        }
                    }
                }
                break;
                case 2: {
                    for (SkuDetails detail:storedList) {
                        if (detail.getSku().equalsIgnoreCase(Constants.productID3)) {
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(this, flowParams);
                        }
                    }
                }
                break;
            }
        } else {
            Log.d(Constants.tag,"Billing : purchaseSubscription ");
        }
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        Log.d(Constants.tag, "onPurchasesUpdated");
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            handlePurchases(list);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(Constants.tag,""+billingResult.getResponseCode()+"--"+BillingClient.BillingResponseCode.USER_CANCELED);
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            Log.d(Constants.tag,""+billingResult.getResponseCode());
            // Handle any other error codes.
        }
    }


    private void handlePurchases(List<Purchase> purchases) {
        for (Purchase skuList:purchases) {
            if (Constants.productID.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED) {
                if (!skuList.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgeParams=AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(skuList.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgeParams, billingResult -> {
                        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK ) {
                            callApiForUpdatePurchase();
                        }
                    });
                }
            }
        }
    }


    int subscriptionPosition = 1;
    public void selectOne(int position){
        subscriptionPosition = position;

        subLayout1.setBackground(getResources().getDrawable(R.drawable.d_round_gray_border));
        subLayout2.setBackground(getResources().getDrawable(R.drawable.d_round_gray_border));
        subLayout3.setBackground(getResources().getDrawable(R.drawable.d_round_gray_border));

        if(position==0){
            subLayout1.setBackground(getResources().getDrawable(R.drawable.d_round_pink_border));
        } else if(position==1){
            subLayout2.setBackground(getResources().getDrawable(R.drawable.d_round_pink_border));
        } else if(position==2){
           subLayout3.setBackground(getResources().getDrawable(R.drawable.d_round_pink_border));
        }

    }


    // when we click the continue btn this method will call
    public void puchaseItem() {
        if(subscriptionPosition == 0){
            purchaseSubscription(0);
            amount = Constants.productIdamount;
            duration = Constants.productIdDuration;
        }else if(subscriptionPosition == 1){
            purchaseSubscription(1);
            amount = Constants.productIdamount2;
            duration = Constants.productIdDuration2;
        } else if(subscriptionPosition == 2){
            purchaseSubscription( 2);
            amount = Constants.productIdamount3;
            duration = Constants.productIdDuration3;
        }
    }


    // when user subscribe the app then this method will call that will store the status of user
    // into the database
    private void callApiForUpdatePurchase() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(getApplicationContext()).getString(Variables.uid,""));
            parameters.put("package","gold");
            parameters.put("amount", amount);
            parameters.put("duration", duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(InAppSubscription_A.this, ApiLinks.startSubscription, parameters, resp -> {
            Functions.getSharedPreference(getApplicationContext()).edit().putBoolean(Variables.isProductPurchase, true).commit();
            String openClassName = getPackageName()+".ActivitiesFragments.Splash_A";
            try {
                startActivity(new Intent(InAppSubscription_A.this, Class.forName(openClassName)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            finish();
        });

    }


    @Override
    protected void onDestroy() {
        billingClient.endConnection();
        super.onDestroy();
    }


    private ViewPager mPager;
    private ArrayList<Integer> ImagesArray;
    public void setSlider(){

        ImagesArray=new ArrayList<>();
        ImagesArray.add(0);
        ImagesArray.add(1);
        ImagesArray.add(2);
        mPager = findViewById(R.id.image_slider_pager);

        try {
            mPager.setAdapter(new SlidingImageAdapter(this, ImagesArray));
        } catch (NullPointerException e){
            e.getCause();
        }

        mPager.setCurrentItem(0);

        TabLayout indicator = findViewById(R.id.indicator);
        indicator.setupWithViewPager(mPager, true);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.purchase_btn) {
            puchaseItem();
        } else if (id == R.id.Goback) {
            onBackPressed();
        } else if (id == R.id.sub_layout1) {
            selectOne(0);
        } else if (id == R.id.sub_layout2) {
            selectOne(1);
        } else if (id == R.id.sub_layout3) {
            selectOne(2);
        }
    }

}
