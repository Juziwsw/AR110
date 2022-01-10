package com.hiar.ar110.adapter;

import android.content.Context;
import android.text.TextUtils;
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
import com.hiar.ar110.listener.OnImageItemClickListener;
import com.hiar.ar110.util.Util;

import java.util.ArrayList;
import java.util.List;

public class PeoCompFaceAdapter extends RecyclerView.Adapter<PeoCompFaceAdapter.ViewHolder> {
    private ArrayList<FaceCompareBaseInfo> mPeopleList = new ArrayList<>();
    private Context mContext;
    private OnImageItemClickListener mOnImageItemClickListener;

    public PeoCompFaceAdapter(Context context, OnImageItemClickListener itemClickListener) {
        mContext = context;
        mOnImageItemClickListener = itemClickListener;
    }

    public void setAdapter(List<FaceCompareBaseInfo> content) {
        if (null == content) {
            return;
        }
        mPeopleList.clear();
        mPeopleList.addAll(content);
        notifyDataSetChanged();
    }

    public void addNewData(List<FaceCompareBaseInfo> content){
        if (null == content) {
            return;
        }
        mPeopleList.addAll(0,content);
        notifyItemRangeInserted(0,content.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recognized_people_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        FaceCompareBaseInfo faceInfo = mPeopleList.get(position);
        if (null == faceInfo) {
            return;
        }

        String datetime = faceInfo.gpsTime;
        String date = datetime.substring(0, 10);
        String time = faceInfo.gpsTime.substring(11, 19);
        String similarity = faceInfo.similarity;
        int score = 92;
        float fscore = 92.0f;
        if (!TextUtils.isEmpty(similarity)) {
            try {
                fscore = Float.parseFloat(similarity);
                if (fscore <= 1.0f) {
                    fscore *= 100;
                }
            } catch (NumberFormatException e) {

            }
            score = (int) fscore;
        }

        holder.mTextDate.setText(date);
        holder.mTextTime.setText(time);
        holder.mConfidence.setText(score + "%");
        String faceUrl = Util.getPeoplePhotoIpHead() + faceInfo.name;
        holder.mImgCrop.setOnClickListener(view -> {
            if (mOnImageItemClickListener != null) {
                mOnImageItemClickListener.onItemClick(faceUrl);
            }
        });
        Glide.with(mContext).load(faceUrl).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImgCrop);
    }

    @Override
    public int getItemCount() {
        return mPeopleList == null ? 0 : mPeopleList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mImgCrop;
        public TextView mTextTime;
        public TextView mTextDate;
        public TextView mConfidence;

        public ViewHolder(View itemView) {
            super(itemView);

            mImgCrop = itemView.findViewById(R.id.img_crop_head);
            mConfidence = itemView.findViewById(R.id.text_score);
            mTextDate = itemView.findViewById(R.id.text_date);
            mTextTime = itemView.findViewById(R.id.text_time);
        }
    }
}
