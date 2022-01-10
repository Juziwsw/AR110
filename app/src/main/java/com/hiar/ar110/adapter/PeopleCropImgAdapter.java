package com.hiar.ar110.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hiar.ar110.R;
import com.hiar.ar110.data.people.FaceCompareBaseInfo;
import com.hiar.ar110.data.people.HistoryFaceUnRecogGroup;
import com.hiar.ar110.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PeopleCropImgAdapter extends RecyclerView.Adapter<PeopleCropImgAdapter.ViewHolder> {
    public ArrayList<FaceCompareBaseInfo> mListData = new ArrayList<>();

    private Context mContext;

    public PeopleCropImgAdapter(Context context) {
        mContext = context;
    }

    public void resetAdapter() {
        mListData.clear();
        notifyDataSetChanged();
    }

    public void setAdapter(HistoryFaceUnRecogGroup content) {
        if (null == content) {
            return;
        }

        mListData.clear();
        Collections.addAll(mListData, content.face);
        notifyDataSetChanged();
    }

    public void setAdapter(List<FaceCompareBaseInfo> faceList) {
        if (null == faceList) {
            return;
        }

        mListData.clear();
        mListData.addAll(faceList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_cropimg_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        if (null != mListData.get(position).gpsTime) {
            String gpsTime = mListData.get(position).gpsTime.substring(11, 19);
            if (null != gpsTime) {
                holder.mTextTime.setText(gpsTime);
            }
        }

        if (null != holder.mImgCropPic) {
            String url = Util.getPeoplePhotoIpHead() + mListData.get(position).name;
            Glide.with(mContext).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImgCropPic);
        }
    }

    @Override
    public int getItemCount() {
        return (null == mListData) ? 0 : mListData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextTime;
        public AppCompatImageView mImgCropPic;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextTime = itemView.findViewById(R.id.text_date_content);
            mImgCropPic = itemView.findViewById(R.id.img_crop_pic);
        }
    }

}

