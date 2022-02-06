package com.qboxus.binder.ActivitiesFragments.Chat;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.qboxus.binderstatic.SimpleClasses.Variables;
import com.qboxus.binderstatic.SimpleClasses.Functions;
import com.qboxus.binder.R;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.CustomViewHolder >{
    public Context context;
    ArrayList<String> gifList = new ArrayList<>();
    private GifAdapter.OnItemClickListener listener;

public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public GifAdapter(Context context, ArrayList<String> urllist, GifAdapter.OnItemClickListener listener) {
        this.context = context;
        this.gifList =urllist;
        this.listener = listener;

    }


    @Override
    public GifAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gif_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(Functions.convertDpToPx(context,104), Functions.convertDpToPx(context,60)));
        GifAdapter.CustomViewHolder viewHolder = new GifAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return gifList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView gif_image;

        public CustomViewHolder(View view) {
            super(view);
            gif_image=view.findViewById(R.id.gif_image);
        }

        public void bind(final String item, final GifAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });


        }

    }

    @Override
    public void onBindViewHolder(final GifAdapter.CustomViewHolder holder, final int i) {
        holder.bind(gifList.get(i),listener);

        Glide.with(context)
                .load(Uri.parse(Variables.gifFirstpart + gifList.get(i)+ Variables.gifSecondpart))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(holder.gif_image);

        Log.d("resp", Variables.gifFirstpart + gifList.get(i)+ Variables.gifSecondpart);

   }


}