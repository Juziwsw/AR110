package com.hiar.ar110.adapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.fragment.AudioUploadFragment;
import com.hiar.ar110.data.audio.AudioFileInfo;
import com.hiar.mybaselib.utils.KeyUtil;

import java.util.ArrayList;
import java.util.List;

public class AudioPlaylistAdapter extends RecyclerView.Adapter<AudioPlaylistAdapter.ViewHolder>{
    private ArrayList<AudioFileInfo> mAudioList = new ArrayList<>();
    private AudioUploadFragment mFragMent;
    private String mAudioRoot = null;

    public void setAdapter(List<AudioFileInfo> content) {
        if(null == content) {
            return;
        }

        mAudioList.clear();
        mAudioList.addAll(content);
        notifyDataSetChanged();
    }

    public AudioPlaylistAdapter(AudioUploadFragment fragment, String audioRoot) {
        mFragMent = fragment;
        mAudioRoot = audioRoot;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_audio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final int position = holder.getAdapterPosition();
        AudioFileInfo audioInfo = mAudioList.get(position);
        String fileName = audioInfo.mFileName;
        String audioName = fileName.substring(0, fileName.indexOf(".mp3"));
        String[] timeArray = audioName.split("_");
        timeArray[1] = timeArray[1].replace("-", ":");
        audioName = timeArray[0] + " " + timeArray[1];
        int total = mAudioList.size();
        holder.mAudioFileName.setText("采集序列"+(total-pos));
        long playLen = audioInfo.mAudioLen;
        int minute = (int)playLen / 60;
        int second = (int)playLen % 60;
        String strAudioLen = String.format("%02d : %02d",minute,second);
        holder.mTextRecordLength.setText(strAudioLen);
        holder.mAudioFileTime.setText(audioName);

        if(audioInfo.mUploadStatus == 0) {
            holder.mImgUploadStatus.setImageResource(R.drawable.icon_not_upload);
        } else {
            holder.mImgUploadStatus.setImageResource(R.drawable.icon_upload_ok);
        }

        KeyUtil.preventRepeatedClick(holder.mItemLayout, view -> {
            if(null != mListener) {
                mListener.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null == mAudioList) ? 0 : mAudioList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mAudioFileName;
        public TextView mTextRecordLength;
        public ImageView mImgUploadStatus;
        public TextView mAudioFileTime;
        public ConstraintLayout mItemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mAudioFileName = itemView.findViewById(R.id.text_audio_name);
            mTextRecordLength = itemView.findViewById(R.id.text_audio_length);
            mImgUploadStatus = itemView.findViewById(R.id.img_upload_status);
            mItemLayout = itemView.findViewById(R.id.layout_audio_item);
            mAudioFileTime = itemView.findViewById(R.id.text_audio_time);
        }
    }

    private  OnAudioItemClickListener mListener = null;

    public void setAudioListener(OnAudioItemClickListener l) {
        mListener = l;
    }

    public interface  OnAudioItemClickListener {
        public void onItemClicked(int position);
    }
}
