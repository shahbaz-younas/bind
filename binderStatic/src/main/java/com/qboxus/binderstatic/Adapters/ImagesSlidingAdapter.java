package com.qboxus.binderstatic.Adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binderstatic.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


/**
 * Created by qboxus on 3/8/2018.
 */

public class ImagesSlidingAdapter extends PagerAdapter {

    private LayoutInflater inflater;
    private List<UserMultiplePhoto> arrayList;

    public ImagesSlidingAdapter(Context context, List<UserMultiplePhoto> image_list) {
        inflater = LayoutInflater.from(context);
        arrayList=image_list;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.item_sliding_image_layout, view, false);
        if(imageLayout!=null) {
            final SimpleDraweeView imageView =  imageLayout.findViewById(R.id.image);
            UserMultiplePhoto model = arrayList.get(position);
            if(model.getImage() != null && !model.getImage().equals("")){
                Uri uri = Uri.parse(Constants.BASE_URL+model.getImage());
                imageView.setImageURI(uri);
            }else {
                Uri uri = Uri.parse(Constants.BASE_URL);
                imageView.setImageURI(uri);
            }
            view.addView(imageLayout, 0);
        }

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