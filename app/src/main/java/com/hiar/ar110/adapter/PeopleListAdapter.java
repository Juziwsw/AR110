package com.hiar.ar110.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hiar.ar110.R;
import com.hiar.ar110.data.people.FaceCompareBaseInfo;

import java.util.ArrayList;
import java.util.List;
import com.hiar.ar110.activity.AR110MainActivity;
import com.hiar.ar110.listener.OnImageItemClickListener;

public class PeopleListAdapter extends RecyclerView.Adapter<com.hiar.ar110.adapter.PeopleListAdapter.ViewHolder> {
    private ArrayList<FaceCompareBaseInfo[]> mPeopleList = new ArrayList<>();
    private OnImageItemClickListener onImageItemClickListener;
    public PeopleListAdapter(OnImageItemClickListener itemClickListener) {
        onImageItemClickListener = itemClickListener;
    }

    public void resetAdapter() {
        mPeopleList.clear();
        notifyDataSetChanged();
    }

    public void setAdapter(List<FaceCompareBaseInfo[]> content) {
        if(null == content) {
            return;
        }

        mPeopleList.clear();
        mPeopleList.addAll(content);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_result_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        FaceCompareBaseInfo[] peoInfo = mPeopleList.get(position);
        if (null == peoInfo) {
            return;
        }

        if(peoInfo != null && (peoInfo.length > 0)) {
            if(!TextUtils.isEmpty(peoInfo[0].faceName)) {
                holder.textPeoName.setText(peoInfo[0].faceName);
            } else {
                holder.textPeoName.setText("");
            }

            if(!TextUtils.isEmpty(peoInfo[0].cardId)) {
                holder.textId.setText(peoInfo[0].cardId);
            } else {
                holder.textId.setText("");
            }

            if(!TextUtils.isEmpty(peoInfo[0].labelName)) {
                holder.textTag.setText(peoInfo[0].labelName);
            } else {
                holder.textTag.setText("");
            }

            ArrayList<FaceCompareBaseInfo> mFaceList = new ArrayList<>();
            for(int j=0; j<peoInfo.length; j++) {
                mFaceList.add(peoInfo[j]);
            }

            if(mFaceList != null && mFaceList.size() > 0) {
                holder.mFaceAdapter.setAdapter(mFaceList);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null == mPeopleList) ? 0 : mPeopleList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPeoName;
        TextView textId;
        TextView textTag;
        RecyclerView recyclerView;
        PeoCompFaceAdapter mFaceAdapter;


        public ViewHolder(View itemView) {
            super(itemView);
            textPeoName = itemView.findViewById(R.id.text_people_name);
            textId = itemView.findViewById(R.id.text_idcard);
            textTag = itemView.findViewById(R.id.text_tag_info);
            recyclerView = itemView.findViewById(R.id.recycler_peop_compare);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL,false));
            mFaceAdapter = new PeoCompFaceAdapter(itemView.getContext().getApplicationContext(),onImageItemClickListener);
            recyclerView.setAdapter(mFaceAdapter);
        }
    }
}


