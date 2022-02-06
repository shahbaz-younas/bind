package com.qboxus.binder_pro.InAppSubscription;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;


import com.qboxus.R;

import java.util.ArrayList;

/**
 * Created by Dinosoftlabs on 10/18/2019.
 */

public class SlidingImageAdapter extends PagerAdapter {

    private ArrayList<Integer> images;
    private LayoutInflater inflater;
    private Context context;


    public SlidingImageAdapter(Context context, ArrayList<Integer> images) {
        this.context = context;
        this.images = images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false);
         ImageView imageView=null;
         TextView textView=null;

       if(imageLayout != null){
            imageView = (ImageView) imageLayout
                   .findViewById(R.id.image);
            textView=imageLayout.findViewById(R.id.textview);

       };

       if(position==0){
           imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_refresh));
           textView.setText(context.getResources().getString(R.string.unlimited_rewinds));

       }

       else if(position==1){
           imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_superlike));
           textView.setText(context.getResources().getString(R.string.free_superlike_a_day));
       }

       else if(position==2){
           imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_boost));
           textView.setText(context.getResources().getString(R.string.unlimited_boost));
       }

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
