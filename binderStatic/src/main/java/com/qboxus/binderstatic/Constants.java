package com.qboxus.binderstatic;

import android.content.Context;

import com.qboxus.binderstatic.Models.CreditModel;
import com.qboxus.binderstatic.Models.FreeVideoCreditModel;
import com.qboxus.binderstatic.Models.PurchaseModel;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public final static String BASE_URL = "";
    public static final String API_KEY = "";


    public final static String LICENCE_URL = "";
    public final static String PRIVACY_POLICY_URL = "";
    public final static String TERMS_OF_SERVICE_URL = "";

    public final static String CONTACT_US_EMAIL = "binderSupport@gmail.com";


    public static final String TWILIO_ACCESS_TOKEN_SERVER = "twilio_access_token_server";


    public static final int UPDATE_INTERVAL = 3000;
    public static final int FATEST_INTERVAL = 3000;
    public static final int DISPLACEMENT = 0;


    public static final boolean CALLING_LIMIT = true;
    public static final int MAX_VIDEO_CALLING_TIME = 60000;
    public static final int MAX_AUDIO_CALLING_TIME = 60000;
    public static int MAX_AUDIO_CALLING_TIME_CREDITS = 0;
    public static int MAX_VIDEO_CALLING_TIME_CREDITS = 0;


    public static final int DEFAULT_DISTANCE =10000;

    public static final int MIN_DEFAULT_AGE = 18;
    public static final int MAX_DEFAULT_AGE = 75;


    public static int BOOST_TIME_DURATION = 1800000;

    public static boolean REMOVE_ADS = false;


    public static String versionname="1.0";

    public static String tag="binder_";

    public static String gifApiKey1 ="gif_api_key";



    public static int BOOST_COINS = 100;

    public static int SUPER_LIKE_COINS = 50;

    public static int VIDEO_CALL_COINS = 50;

    public static int AUDIO_CALL_COINS = 30;

    public static boolean GET_COINS_FROM_VIDEOS = true;



    public static String productID ="android.test.purchased";
    public static String productIdDuration ="1";
    public static String productIdamount ="19.99";
    public static String productID2 ="android.test.purchased";
    public static String productIdDuration2 ="3";
    public static String productIdamount2 ="29.99";
    public static String productID3 ="android.test.purchased";
    public static String productIdDuration3 ="12";
    public static String productIdamount3 ="69.99";


    public static String boostID = "android.test.purchased";
    public static String boostNumber = "1";
    public static String boostIdamount = "6.99";
    public static String boostID2 = "android.test.purchased";
    public static String boostNumber2 = "5";
    public static String boostIdamount2 = "5.99";
    public static String boostID3 = "android.test.purchased";
    public static String boostNumber3 = "10";
    public static String boostamount3 = "4.99";






    //if you want to add more packages then add item in list
    public static List<FreeVideoCreditModel> freeCreditsVideosList() {
        List<FreeVideoCreditModel> list = new ArrayList<>();

        FreeVideoCreditModel model1 = new FreeVideoCreditModel();

        model1.setPackageId("1");
        model1.setCredits("200");
        model1.setNoOfVideos("10");
        model1.setViewNoOfVideos("0");

        list.add(model1);

        return list;

    }


    //if you want to increase or decrease credits packages then add or remove item from below list
    public static List<CreditModel> creditsPackagesList(Context context) {
        List<CreditModel> list = new ArrayList<>();

        if(GET_COINS_FROM_VIDEOS){
            CreditModel model = new CreditModel();

            model.setCoinsPurchaseId("");
            model.setCoinsNumber(context.getString(R.string.free));
            model.setCoinsAmount(context.getString(R.string.credits_capital));
            model.setCoinsImage(R.drawable.ic_free);

            list.add(model);
        }

        CreditModel model1 = new CreditModel();

        //here you will add purchase id which you will get from your play store
        model1.setCoinsPurchaseId("android.test.purchased");
        model1.setCoinsNumber("100");
        model1.setCoinsAmount("$3.99");
        model1.setCoinsImage(R.drawable.ic_coins_two);

        list.add(model1);
        //if you want to remove item then uncomment below line
        //list.remove(model1);

        CreditModel model2 = new CreditModel();

        model2.setCoinsPurchaseId("android.test.purchased");
        model2.setCoinsNumber("550");
        model2.setCoinsAmount("$13.99");
        model2.setCoinsImage(R.drawable.ic_coins3);

        list.add(model2);

        CreditModel model3 = new CreditModel();

        model3.setCoinsPurchaseId("android.test.purchased");
        model3.setCoinsNumber("1250");
        model3.setCoinsAmount("$20.99");
        model3.setCoinsImage(R.drawable.ic_coins_muktiple);

        list.add(model3);

        return list;

    }

    public static String coinsPurchaseID1 = "android.test.purchased";

}
