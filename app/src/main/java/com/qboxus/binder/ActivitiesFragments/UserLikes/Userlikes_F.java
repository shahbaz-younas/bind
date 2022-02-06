package com.qboxus.binder.ActivitiesFragments.UserLikes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.qboxus.binderstatic.Models.MatchModel;
import com.qboxus.binderstatic.Models.NearbyUserModel;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binder.ActivitiesFragments.Matchs.Match_F;
import com.qboxus.binder.ActivitiesFragments.Users.SuperLike_Popup_F;
import com.qboxus.binderstatic.Adapters.UserLikeAdapter;
import com.qboxus.binderstatic.ApiClasses.ApiLinks;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.AdapterClickListener;
import com.qboxus.binderstatic.ApiClasses.ApiRequest;
import com.qboxus.binderstatic.Interfaces.Callback;
import com.qboxus.binderstatic.Interfaces.FragmentCallback;
import com.qboxus.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.qboxus.binder.R;
import com.qboxus.binder.ActivitiesFragments.Users.UserDetail_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class Userlikes_F extends RootFragment implements View.OnClickListener{

    View view;
    Context context;

    ArrayList<NearbyUserModel> dataList;
    RecyclerView recyclerView;
    UserLikeAdapter adapter;

    ProgressBar progressBar;

    JSONArray likedUserArray = new JSONArray();
    Boolean isViewCreated =false;

    int wallet;

    Date c;
    SimpleDateFormat df;
    int currentYear;
    Integer todayDay = 0;

    public Userlikes_F() {
        //Required Empty Constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_user_list, container, false);
        context=getContext();

        Calendar cal = Calendar.getInstance();
        todayDay = cal.get(Calendar.DAY_OF_MONTH);
        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        currentYear = Integer.parseInt(df.format(c));

        wallet = Integer.parseInt(Functions.getSharedPreference(context).getString(Variables.uWallet, "0"));

        getScreenSize();

        progressBar =view.findViewById(R.id.progress_bar);

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new GridLayoutManager(context,2));
        recyclerView.setHasFixedSize(false);
        dataList = new ArrayList<>();
        adapter=new UserLikeAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(int pos, Object item, View view) {
                if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                    openUserDetail((NearbyUserModel) item,pos);
                }
            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {

            }
        });
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        view.findViewById(R.id.subscribe_btn).setOnClickListener(this);


        view.findViewById(R.id.cardview).
                setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        isViewCreated = true;

        return view;
    }


    public void getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Variables.screenHeight = displayMetrics.heightPixels;
        Variables.screenWidth = displayMetrics.widthPixels;
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN |ItemTouchHelper.UP) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                return super.getSwipeDirs(recyclerView, viewHolder);
            }else {
                return 0;
            }
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();

            NearbyUserModel item= dataList.get(position);
            dataList.remove(position);
            if(swipeDir==8){
                likeDislike("1", "0", position, item);
            }else if(swipeDir==4){
                likeDislike("0", "0", position, item);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if(dX<0.0){
                viewHolder.itemView.findViewById(R.id.left_overlay).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.right_overlay).setVisibility(View.GONE);
            }else if(dX>0.0) {
                viewHolder.itemView.findViewById(R.id.left_overlay).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.right_overlay).setVisibility(View.VISIBLE);
            }else {
                viewHolder.itemView.findViewById(R.id.left_overlay).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.right_overlay).setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if((isViewCreated && menuVisible)){

            wallet = Integer.parseInt(Functions.getSharedPreference(context).getString(Variables.uWallet, "0"));
            getPeopleNearby();

            if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                itemTouchHelper.attachToRecyclerView(recyclerView);
                view.findViewById(R.id.subscribe_btn).setVisibility(View.GONE);
            }else {
                view.findViewById(R.id.subscribe_btn).setVisibility(View.VISIBLE);
                view.findViewById(R.id.subscribe_btn).setOnClickListener(this);
            }
        }
    }


    private void getPeopleNearby() {
        if(progressBar.getVisibility() == View.GONE){
            progressBar.setVisibility(View.VISIBLE);
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid,""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.showUserLikes, parameters, new Callback() {
            @Override
            public void response(String resp) {
                if(progressBar.getVisibility() == View.VISIBLE){
                    progressBar.setVisibility(View.GONE);
                }
                parseUserInfo(resp);
            }
        });
    }


    public void parseUserInfo(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                dataList.clear();
                JSONArray msg = jsonObject.getJSONArray("msg");

                for (int i=0; i<msg.length();i++){
                    JSONObject userobj = msg.getJSONObject(i).optJSONObject("LikeUser");
                    JSONObject userdata;
                    if(userobj.optString("user_id").equals(Functions
                            .getSharedPreference(context).getString(Variables.uid,""))){
                        userdata = msg.getJSONObject(i).optJSONObject("OtherUser");
                    }else {
                        userdata = msg.getJSONObject(i).optJSONObject("User");
                    }

                    NearbyUserModel item = new NearbyUserModel();

                    JSONArray userImagesList = userdata.optJSONArray("UserImage");
                    JSONArray userPassionsList = userdata.optJSONArray("UserPassion");
                    if(userImagesList != null && userImagesList.length()>0){
                        item.imagesUrl = new ArrayList<>();
                        for(int k = 0; k<6; k++){
                            UserMultiplePhoto model = new UserMultiplePhoto();
                            if(k<userImagesList.length()){
                                model.setImage(userImagesList.optJSONObject(k).optString("image"));
                                model.setId(userImagesList.optJSONObject(k).optString("id"));
                                model.setOrderSequence(Integer.parseInt(userImagesList.optJSONObject(k).optString("order_sequence")));
                                item.imagesUrl.add(k, model);
                            }else {
                                model.setOrderSequence(k);
                                item.imagesUrl.add(k, model);
                            }
                        }
                    }

                    item.setFbId(userdata.optString("id"));
                    item.setFirstName(userdata.optString("first_name"));
                    item.setLastName(userdata.optString("last_name"));
                    item.setName(userdata.optString("first_name")+" "+userdata.optString("last_name"));
                    item.setJobTitle(userdata.optString("job_title"));
                    item.setCompany(userdata.optString("company"));
                    if(userdata.optJSONObject("School") != null &&
                            userdata.optJSONObject("School").optString("name") != null){
                        item.setSchool(userdata.optJSONObject("School").optString("name"));
                    }

                    if(!userdata.optString("dob").equals("0000-00-00")){
                        try {
                            Date date = df.parse(userdata.optString("dob"));
                            int age = Integer.parseInt(df.format(date));
                            item.setBirthday(" " + (currentYear - age));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    item.setHide_location(""+userdata.optString("hide_location"));
                    item.setAbout(userdata.optString("bio"));
                    item.setGender(userdata.optString("gender"));
                    item.setGenderShow(userdata.optString("gender_show"));
                    item.setLike(userdata.optString("like"));
                    item.setSuperLike(userdata.optString("super_like"));

                    if(userobj != null){
                        item.setLike(userobj.getString("like"));
                        item.setSuperLike(userobj.getString("super_like"));
                    }

                    if(userPassionsList != null && userPassionsList.length()>0){
                        item.userPassion = new ArrayList<>();
                        for(int j = 0; j<userPassionsList.length(); j++){
                            if(userPassionsList.optJSONObject(i) != null){
                                item.userPassion.add(userPassionsList.optJSONObject(i)
                                        .optJSONObject("Passion").optString("title"));
                            }
                        }
                    }

                    dataList.add(item);
                }

                if(dataList.isEmpty()){
                    view.findViewById(R.id.nodata_found_txt).setVisibility(View.VISIBLE);
                }else {
                    view.findViewById(R.id.nodata_found_txt).setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();

            }else {
                view.findViewById(R.id.nodata_found_txt).setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            view.findViewById(R.id.nodata_found_txt).setVisibility(View.VISIBLE);
        }

    }


    public void openUserDetail(NearbyUserModel item, int pos){
        UserDetail_F userDetail_f = new UserDetail_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        getActivity().getSupportFragmentManager().setFragmentResultListener("1212",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if(result != null){
                            String status = result.getString("status");
                            int position = result.getInt("pos");
                            NearbyUserModel model = new NearbyUserModel();
                            if(result.getSerializable("data") != null){
                                model = (NearbyUserModel) result.getSerializable("data");
                            }

                            switch (status){
                                case "0":
                                    likeDislike("0", "0", position, model);
                                    dataList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    break;
                                case "1":
                                    if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                                        likeDislike("1","0", position, model);
                                        dataList.remove(position);
                                        adapter.notifyDataSetChanged();
                                    }else {
                                        if(Functions.getSharedPreference(context).getBoolean(Variables.userLikeLimit, false) && checkDate()){
                                            openSubscriptionView();
                                        }else if(Functions.getSharedPreference(context).getBoolean(Variables.userLikeLimit, false) && !checkDate()){
                                            likeDislike("1","0", position, model);
                                            dataList.remove(position);
                                            adapter.notifyDataSetChanged();
                                        }else if(!Functions.getSharedPreference(context).getBoolean(Variables.userLikeLimit, false)){
                                            likeDislike("1","0", position, model);
                                            dataList.remove(position);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                    break;
                                case "2":
                                    if(Functions.getSharedPreference(context).getBoolean(Variables.userSuperlikeLimit, false) && checkDate()){
                                        if(wallet == Constants.SUPER_LIKE_COINS || wallet > Constants.SUPER_LIKE_COINS){
                                            callApiForUseCoins(position, model);
                                        }else {
                                            openSuperLikePopup(position);
                                        }
                                    }else if(Functions.getSharedPreference(context).getBoolean(Variables.userSuperlikeLimit, false) && !checkDate()){
                                        likeDislike("0","1", position, model);
                                        dataList.remove(position);
                                        adapter.notifyDataSetChanged();
                                    }else if(!Functions.getSharedPreference(context).getBoolean(Variables.userSuperlikeLimit, false)){
                                        likeDislike("0","1", position, model);
                                        dataList.remove(position);
                                        adapter.notifyDataSetChanged();
                                    }
                                    break;
                            }
                        }
                    }
                });
        Bundle args = new Bundle();
        args.putSerializable("data", item);
        args.putString("from_where", "user_list");
        args.putInt("pos", pos);
        userDetail_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, userDetail_f).commit();

    }

    private void likeDislike(String like, String superLike, int position, NearbyUserModel item) {
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid,""));
            userObject.put("other_user_id", item.fbId);
            userObject.put("super_like", superLike);
            userObject.put("like", like);
            likedUserArray.put(userObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(superLike.equals("1")){
            callApiLikeDislikeUser(position, item);
            likedUserArray = new JSONArray();
        }else if(likedUserArray.length()>2){
            callApiLikeDislikeUser(position, item);
            likedUserArray = new JSONArray();
        }

        if(like.equals("1") || superLike.equals("1")){
            if(item.getLike().equals("1") || item.getSuperLike().equals("1")){
                callApiMatchUser(item);
            }
        }

    }

    // below two method is used get the user pictures and about text from our server
    private void callApiLikeDislikeUser(int position, NearbyUserModel model) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("like_data", likedUserArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.likeUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    String msg = jsonObject.optString("msg");

                    if (code.equals("201") && msg.contains("limit reached per day. please subscribe")) {
                        if(wallet == Constants.SUPER_LIKE_COINS || wallet > Constants.SUPER_LIKE_COINS){
                            callApiForUseCoins(position,model);
                        }else {
                            openSuperLikePopup(position);
                        }
                        Date c = Calendar.getInstance().getTime();
                        final String formattedDate = Variables.df.format(c);
                        Functions.getSharedPreference(context).edit().putBoolean(Variables.userSuperlikeLimit, true).commit();
                        Functions.getSharedPreference(context).edit().putString(Variables.userLikeLimitDate, formattedDate).commit();
                    }
                    else if(code.equals("201") && msg.contains("limit reached per day. please subscribe for unlimited likes")){
                        Functions.getSharedPreference(context).edit().putBoolean(Variables.userLikeLimit, true).commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // below two method is used get the user pictures and about text from our server
    private void callApiMatchUser(NearbyUserModel model) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid,""));
            parameters.put("other_user_id", model.getFbId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.matchUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                try {
                    JSONObject object = new JSONObject(resp);
                    if(object.optString("code").equals("200")){
                        MatchModel match_model =new MatchModel();
                        match_model.setU_id(model.getFbId());
                        match_model.setUsername(model.getFirstName());
                        match_model.setSuperLike(model.getSuperLike());
                        if(model.getImagesUrl() != null){
                            match_model.setPicture(model.getImagesUrl().get(0).getImage());
                        }
                        openMatch(match_model);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean checkDate(){
        long currenttime = System.currentTimeMillis();

        Log.d(Constants.tag, "checkDate");

        //database date in millisecond
        long compareDate = 0;
        Date d = null;
        try {
            d = Variables.df.parse(Functions.getSharedPreference(context).getString(Variables.userLikeLimitDate,""));
            compareDate = d.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long difference = currenttime - compareDate;
        if (difference < 86400000) {
            Log.d(Constants.tag, "(difference < 86400000)");
            int chatday = Integer.parseInt(Functions.getSharedPreference(context).getString(Variables.userLikeLimitDate,"").substring(0, 2));

            Log.d(Constants.tag, "chatday="+chatday);
            if (todayDay == chatday){
                Log.d(Constants.tag, "Date = Today");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.subscribe_btn:
                openSubscriptionView();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if((isViewCreated)){
            getPeopleNearby();

            if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                itemTouchHelper.attachToRecyclerView(recyclerView);
                view.findViewById(R.id.subscribe_btn).setVisibility(View.GONE);
            }else {
                view.findViewById(R.id.subscribe_btn).setVisibility(View.VISIBLE);
                view.findViewById(R.id.subscribe_btn).setOnClickListener(this);
            }
        }
    }

    private void callApiForUseCoins(int position, NearbyUserModel model) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.uid, ""));
            parameters.put("coin", ""+Constants.SUPER_LIKE_COINS);
            parameters.put("feature", "superlike");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.useCoin, parameters, new Callback() {
            @Override
            public void response(String resp) {

                try {
                    JSONObject jsonObject = new JSONObject(resp);

                    String code = jsonObject.optString("code");
                    if(code.equals("200")){
                        JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");

                        wallet = Integer.parseInt(userObject.getString("wallet"));
                        Functions.getSharedPreference(context).edit()
                                .putString(Variables.uWallet, userObject.getString("wallet")).apply();

                        likeDislike("0","1", position, model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // when a match is build between user then this method is call and open the view if match screen
    public void openMatch(MatchModel item){
        Match_F matchF = new Match_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putSerializable("data",item);
        matchF.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, matchF).commit();
    }

    public void openSubscriptionView(){
        try {
            startActivity(new Intent(getActivity(), Class.forName("com.qboxus.binder_pro.InAppSubscription.InAppSubscription_A")));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openSuperLikePopup(int position){
        SuperLike_Popup_F superLike_popup_f = new SuperLike_Popup_F(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {
                if(bundle != null){
                    if(bundle.getBoolean("superlike", false)){
                        dataList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null).replace(R.id.MainMenuFragment, superLike_popup_f).commit();
    }
}
