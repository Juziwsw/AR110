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
import com.hiar.ar110.data.vehicle.RecoRecog;
import com.hiar.ar110.util.Util;

import java.util.ArrayList;
import java.util.List;


public class CarCropImgAdapter extends RecyclerView.Adapter<CarCropImgAdapter.ViewHolder> {
    public ArrayList<RecoRecog> mDateData = new ArrayList<>();
    private Context mContext;

    public CarCropImgAdapter(Context context) {
        mContext = context;
    }

    public void resetAdapter() {
        mDateData.clear();
        notifyDataSetChanged();
    }

    public void setAdapter(List<RecoRecog> content, final String strDate) {
        if (null == content) {
            return;
        }

        mDateData.clear();

        ArrayList<RecoRecog> filterData = new ArrayList<>();
        int len = content.size();
        for (int i = 0; i < len; i++) {
            if (content.get(i).gpsTime.startsWith(strDate)) {
                filterData.add(content.get(i));
            }
        }

        mDateData.addAll(filterData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CarCropImgAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_cropimg_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        if (null != mDateData.get(position).gpsTime) {
            String gpsTime = mDateData.get(position).gpsTime.substring(11);
            if (null != gpsTime) {
                holder.mTextTime.setText(gpsTime);
            }
        }

        if (null != holder.mImgCropPic) {
            String url = Util.getCarPhotoIpHead() + mDateData.get(position).url;
            Glide.with(mContext).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImgCropPic);
        }
    }

    @Override
    public int getItemCount() {
        return (null == mDateData) ? 0 : mDateData.size();
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

