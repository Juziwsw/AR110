package com.hiar.ar110.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.fragment.PeopleResultFragment;
import com.hiar.mybaselib.utils.KeyUtil;

import java.util.ArrayList;
import java.util.List;

public class PeopleDateListAdapter extends RecyclerView.Adapter<PeopleDateListAdapter.ViewHolder>{
    private ArrayList<String> mDateData = new ArrayList<>();
    private PeopleResultFragment mFragMent;

    public void resetAdapter() {
        mDateData.clear();
        notifyDataSetChanged();
    }

    public void setAdapter(List<String> content) {
        if(null == content) {
            return;
        }

        mDateData.clear();
        mDateData.addAll(content);
        notifyDataSetChanged();
    }

    public PeopleDateListAdapter(PeopleResultFragment fragment) {
        mFragMent = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_date_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        holder.mTextDate.setText(mDateData.get(position));
        KeyUtil.preventRepeatedClick(holder.mTextDate, view -> mFragMent.setNewDate(mDateData.get(position)));
    }

    @Override
    public int getItemCount() {
        return (null == mDateData) ? 0 : mDateData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextDate = itemView.findViewById(R.id.text_date_content);
        }
    }
}
