package com.qboxus.binderstatic.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.qboxus.binderstatic.Models.NearbyUserModel;
import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binderstatic.R;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.Interfaces.AdapterClickListener;
import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by qboxus on 3/20/2018.
 */

public class UserLikeAdapter extends RecyclerView.Adapter<UserLikeAdapter.CustomViewHolder >{
    public Context context;
    ArrayList<NearbyUserModel> dataList = new ArrayList<>();
    AdapterClickListener adapterClickListener;

    Integer todayDay =0;
    int width;

    String currentTime;

    public UserLikeAdapter(Context context, ArrayList<NearbyUserModel> user_dataList, AdapterClickListener adapterClickListener) {

        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         currentTime =dateFormat.format(calendar.getTime());

        this.context = context;
        this.dataList=user_dataList;

        width=(Variables.screenWidth /2)-20;
        // get the today as a integer number to make the dicision the chat date is today or yesterday
        Calendar cal = Calendar.getInstance();
        todayDay = cal.get(Calendar.DAY_OF_MONTH);

        this.adapterClickListener=adapterClickListener;
    }

    @Override
    public UserLikeAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_layout3,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(width, (Variables.screenWidth - 300)));
        UserLikeAdapter.CustomViewHolder viewHolder = new UserLikeAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return dataList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView name, age, distanceText;
        public SimpleDraweeView image;
        public FrameLayout leftOverlay, rightOverlay;

        ImageView superLikeImage;

        public CustomViewHolder(View view) {
            super(view);
            name=view.findViewById(R.id.username);
            image=view.findViewById(R.id.image);
            superLikeImage =view.findViewById(R.id.supperlike_img);
            distanceText =view.findViewById(R.id.distance_txt);
            leftOverlay =view.findViewById(R.id.left_overlay);
            rightOverlay =view.findViewById(R.id.right_overlay);
        }

        public void bind(final int pos,final NearbyUserModel item,
                         final AdapterClickListener adapterClickListener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterClickListener.onItemClick(pos,item,v);
                }
            });
        }
    }


    @Override
    public void onBindViewHolder(final UserLikeAdapter.CustomViewHolder holder, final int i) {

        final NearbyUserModel item=dataList.get(i);

        holder.bind(i,item,adapterClickListener);

        if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
            holder.name.setText(item.getFirstName());
            holder.distanceText.setText(item.getLocation());
        }else {
            holder.name.setText("");
        }

        if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
            if(item.imagesUrl != null ){
                Collections.sort(item.imagesUrl, new Comparator<UserMultiplePhoto>() {
                    @Override public int compare(UserMultiplePhoto p1, UserMultiplePhoto p2) {
                        return p1.getOrderSequence() - p2.getOrderSequence(); // Ascending
                    }
                });

                UserMultiplePhoto model = item.imagesUrl.get(0);
                if(model.getImage() != null && !model.getImage().equals("")){
                    Uri uri = Uri.parse(Constants.BASE_URL+model.getImage());
                    holder.image.setImageURI(uri);
                }
            }
        }else {
            if(item.imagesUrl != null && !Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase, false)){
                Collections.sort(item.imagesUrl, new Comparator<UserMultiplePhoto>() {
                    @Override public int compare(UserMultiplePhoto p1, UserMultiplePhoto p2) {
                        return p1.getOrderSequence() - p2.getOrderSequence(); // Ascending
                    }
                });

                UserMultiplePhoto model = item.imagesUrl.get(0);

                if(!model.getImage().equals("")){
                    Glide.with(context)
                            .load(Constants.BASE_URL+model.getImage())
                            .transform(new MultiTransformation<Bitmap>(new BlurTransformation(50), new CenterCrop()))
                            .placeholder(R.drawable.ic_user_icon)
                            .error(R.drawable.ic_user_icon)
                            .into(holder.image);
                }
            }
        }

        if(item.getSuperLike().equals("1")){
            holder.superLikeImage.setVisibility(View.VISIBLE);
        }else {
            holder.superLikeImage.setVisibility(View.GONE);
        }

        holder.rightOverlay.setVisibility(View.GONE);
        holder.leftOverlay.setVisibility(View.GONE);
   }
}