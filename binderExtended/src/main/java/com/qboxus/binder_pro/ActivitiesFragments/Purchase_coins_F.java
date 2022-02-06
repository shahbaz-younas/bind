package com.qboxus.binder_pro.ActivitiesFragments;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Interfaces.AdapterClickListener;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.Models.CreditModel;
import com.qboxus.binderstatic.Models.PurchaseCoinsSliderModel;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.GridSpacingItemDecoration;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.R;
import com.qboxus.binder_pro.Adapters.CreditsAdapter;
import com.qboxus.binder_pro.Adapters.PurchaseCoinsSlidingAdapter;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class Purchase_coins_F extends RootFragment implements View.OnClickListener, PurchasesUpdatedListener {

    View view;
    Context context;

    RecyclerView rv;
    CreditsAdapter adapter;

    CreditModel model = new CreditModel();
    public static String selectedCoins = "";

    int position=2;

    TextView coin_count_txt,continueButton;
    RelativeLayout continueButtonView;
    ViewPager viewPager;
    WormDotsIndicator dotsIndicator;
    List<PurchaseCoinsSliderModel> data_list;
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 5000; //delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000;


    String purchaseId="", coins="", price="";

    List<SkuDetails> storedList = new ArrayList<>();
    BillingClient billingClient;

    public Purchase_coins_F() {
        //Required Empty
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_purchase, container, false);
        context=getContext();

        initializeViews();
        initlize_billing();

        return view;
    }


    private void initializeViews() {
        view.findViewById(R.id.Goback).setOnClickListener(this);

        coin_count_txt = view.findViewById(R.id.coin_count_txt);
        coin_count_txt.setText(Functions.getSharedPreference(context).getString(Variables.uWallet,""));

        rv = view.findViewById(R.id.rv);

        setAdapterToRecyclerView();

        viewPager = view.findViewById(R.id.viewPager);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        setSlider();
        handler();

        continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);
        continueButtonView = view.findViewById(R.id.continueButtonView);
    }


    private void setAdapterToRecyclerView() {
        GridLayoutManager layout = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layout);
        rv.setHasFixedSize(false);

        adapter = new CreditsAdapter(getContext(), Constants.creditsPackagesList(context), new AdapterClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(int pos, Object object, View view) {
                model = (CreditModel) object;
                position = pos;
                Constants.creditsPackagesList(context).remove(position);
                selectedCoins = model.getCoinsNumber();
                Constants.creditsPackagesList(context).add(position,model);
                adapter.notifyDataSetChanged();

                if(Constants.GET_COINS_FROM_VIDEOS && pos == 0){
                    openFreeCreditsVideoFragment();
                    continueButton.setTextColor(ContextCompat.getColor(context, R.color.gray));
                    continueButtonView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_google_background));
                }else {
                    position = pos+1;
                    purchaseId = model.getCoinsPurchaseId();
                    coins = model.getCoinsNumber();
                    price = model.getCoinsAmount();
                    continueButton.setTextColor(ContextCompat.getColor(context, R.color.white));
                    continueButtonView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_pink_background));
                }
            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {
            }
        });

        rv.setAdapter(adapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen._10sdp);
        rv.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
    }


    private void setSlider() {
        data_list = new ArrayList<>();
        data_list.add(new PurchaseCoinsSliderModel(context.getString(R.string.boost), context.getString(R.string.boostDescription)+" "+Constants.BOOST_COINS+" "+context.getString(R.string.boostDescription1), R.drawable.ic_boost_purchase));
        data_list.add(new PurchaseCoinsSliderModel(context.getString(R.string.videocall), context.getString(R.string.videocallDescription)+" "+Constants.VIDEO_CALL_COINS+" "+context.getString(R.string.videocallDescription1), R.drawable.ic_video_call_purchase));
        data_list.add(new PurchaseCoinsSliderModel(context.getString(R.string.superlike), context.getString(R.string.superlikeDescription), R.drawable.ic_superlike_purchase));

        viewPager.setAdapter(new PurchaseCoinsSlidingAdapter(context, data_list));
        dotsIndicator.setViewPager(viewPager);
    }


    private void handler() {
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == data_list.size()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.Goback) {
            getActivity().onBackPressed();
        } else if (id == R.id.continueButton) {
            Log.d(Constants.tag, "purchaseId: " + purchaseId);
            if (!purchaseId.equals("")) {
                purchaseItem();
            }
        }
    }

    public void initlize_billing(){
        billingClient=BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        Log.d(Constants.tag,"Billing : connection establish ");
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d(Constants.tag,"Billing : onBillingSetupFinished ");
                if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
                    Log.d(Constants.tag,"Billing : Load purchase query "+billingResult.getResponseCode());

                    InitPurchases();
                }
                else {
                    Log.d(Constants.tag,"Billing : onBillingSetupFinished "+billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(Constants.tag,"Billing : onBillingServiceDisconnected");
            }
        });

    }

    private void InitPurchases() {
        final List<String> skuList=new ArrayList<>();
        skuList.add(Constants.coinsPurchaseID1);

        SkuDetailsParams.Builder params=SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);

        params.setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build()
                , new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {

                        storedList.clear();
                        storedList.addAll(list);
                        Log.d(Constants.tag,"Billing : onSkuDetailsResponse ");
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK && purchases!=null ) {
            Log.d(Constants.tag,"Billing : handle purchase okay");
            handlePurchases(purchases);
        }
        else if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(Constants.tag,"Billing : handle purchase cancel");
        }
        else {
            Log.d(Constants.tag,"Billing : handle purchase other "+billingResult.getDebugMessage());
        }
    }

    private void handlePurchases(List<Purchase> purchases) {
        for (Purchase skuList : purchases) {
            Log.d(Constants.tag,"Billing : purchase "+skuList.getSkus().get(0)+" acknowledged status "+skuList.isAcknowledged()+" purchase status "+(skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED));

            for (int i = 0; i<Constants.creditsPackagesList(context).size(); i++){
                CreditModel model = Constants.creditsPackagesList(context).get(i);
                if(model.getCoinsPurchaseId().equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED){
                    if (!skuList.isAcknowledged()) {

                        Log.d(Constants.tag,"Consume check : "+(!skuList.isAcknowledged()));
                        ConsumeParams consumeParams =
                                ConsumeParams.newBuilder()
                                        .setPurchaseToken(skuList.getPurchaseToken())
                                        .build();

                        ConsumeResponseListener listener = new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK ) {
                                    Log.d(Constants.tag,model.getCoinsPurchaseId()+"---"+model.getCoinsNumber());
                                    callApiPurchaseCoins(skuList.getPurchaseToken());
                                }
                            }
                        };
                        billingClient.consumeAsync(consumeParams, listener);
                    }
                    break;
                }
            }
        }
    }

    private void callApiPurchaseCoins(String purchaseId) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid,""));
            parameters.put("title", coins+" coins");
            parameters.put("coin", coins);
            parameters.put("price", price);
            parameters.put("purchase_id", purchaseId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                                coin_count_txt.setText(userObject.optString("wallet"));
                                Functions.getSharedPreference(context).edit()
                                        .putString(Variables.uWallet, userObject.optString("wallet")).commit();
                                Log.d(Constants.tag, "Purchase Coins: "+Functions
                                        .getSharedPreference(context).getString(Variables.uWallet,""));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    public void purchaseItem() {
        if (billingClient.isReady()) {
            for (SkuDetails detail:storedList) {
                if (detail.getSku().equalsIgnoreCase(purchaseId)) {
                    Log.d(Constants.tag,"purchaseSubscription position "+coins+" "+detail.getSku()+" "+detail.getPrice());
                    BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                            .setSkuDetails(detail)
                            .build();
                    billingClient.launchBillingFlow(getActivity(), flowParams);
                }
            }
        }
        else {
            Log.d(Constants.tag,"Billing : purchaseSubscription ");
        }
    }

    public void openFreeCreditsVideoFragment(){
        purchaseId="";
        FreeCreditsVideo_F freeCreditsVideoF = new FreeCreditsVideo_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        getActivity().getSupportFragmentManager().setFragmentResultListener("1222",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if(requestKey.equals("1222")){
                            coin_count_txt.setText(Functions.getSharedPreference(context).getString(Variables.uWallet,""));
                        }
                    }
                });
        transaction.addToBackStack(null).replace(R.id.purchaseFragment, freeCreditsVideoF).commit();
    }

    @Override
    public void onDetach() {
        selectedCoins = "";
        billingClient.endConnection();
        super.onDetach();
    }
}
