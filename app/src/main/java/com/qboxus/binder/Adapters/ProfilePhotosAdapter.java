package com.qboxus.binder.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.qboxus.binderstatic.Constants;
import com.qboxus.binderstatic.Models.UserMultiplePhoto;
import com.qboxus.binder.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wonshinhyo.dragrecyclerview.DragAdapter;
import com.wonshinhyo.dragrecyclerview.DragHolder;
import com.wonshinhyo.dragrecyclerview.DragRecyclerView;

import java.util.List;

/**
 * Created by qboxus on 7/16/2018.
 */

public class ProfilePhotosAdapter extends DragAdapter {

    Context context;
    List<UserMultiplePhoto> photos;
    boolean isFromRegister;

    private ProfilePhotosAdapter.OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(UserMultiplePhoto item, int postion, View view);
    }


    public ProfilePhotosAdapter(Context context, List<UserMultiplePhoto> arrayList, boolean isFromRegister, ProfilePhotosAdapter.OnItemClickListener listener)  {
        super(context,arrayList);
        this.context=context;
        photos=arrayList;
        this.isFromRegister = isFromRegister;
        this.listener=listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        return new HistoryviewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_edit_profile_layout, viewGroup, false));
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }


    @Override
    public void onBindViewHolder(final DragRecyclerView.ViewHolder hol, final int position) {
        super.onBindViewHolder(hol, position);
        HistoryviewHolder holder = (HistoryviewHolder) hol;

        UserMultiplePhoto model = photos.get(position);

        if(position == 0 && model.getImage() != null){
            if(isFromRegister){
                holder.crossButton.setVisibility(View.GONE);
                holder.addButton.setVisibility(View.VISIBLE);
                holder.addButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_edit_pink));
                holder.image.setImageURI("data:image/png;base64,"+model.getImage(), null);
            }else if(!isFromRegister){
                holder.crossButton.setVisibility(View.GONE);
                holder.addButton.setVisibility(View.GONE);
                Uri uri;
                uri = Uri.parse(Constants.BASE_URL+model.getImage());
                holder.image.setImageURI(uri);
            }
        }

        if(model.getImage() == null || model.getImage().equals("")){
            holder.addButton.setVisibility(View.VISIBLE);
            holder.addButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_round_add_btn));
            holder.crossButton.setVisibility(View.GONE);

            Uri uri = Uri.parse("null");
            holder.image.setImageURI(uri);

        }else if(position != 0 && !model.getImage().equals("")){
            holder.crossButton.setVisibility(View.VISIBLE);
            holder.addButton.setVisibility(View.GONE);
            if(isFromRegister){
                holder.image.setImageURI("data:image/png;base64,"+model.getImage(), null);
            }else if(!isFromRegister){
                holder.crossButton.setVisibility(View.VISIBLE);
                holder.addButton.setVisibility(View.GONE);
                Uri uri = Uri.parse(Constants.BASE_URL+model.getImage());
                holder.image.setImageURI(uri);
            }
        }

        holder.bind(model,position,listener);
     }

    /**
     * Inner Class for a recycler view
     */
    class HistoryviewHolder extends DragHolder {
        View view;
        SimpleDraweeView image;
        ImageButton crossButton,addButton;
        public HistoryviewHolder(View itemView) {
            super(itemView);
            view = itemView;
            image=view.findViewById(R.id.image);
            addButton =view.findViewById(R.id.add_btn);
            crossButton =view.findViewById(R.id.cross_btn);
        }


        public void bind(final UserMultiplePhoto item, final int position , final ProfilePhotosAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item,position,v);
                }
            });

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item,position,v);
                }
            });

            crossButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item,position,v);
                }
            });
        }

    }

}

