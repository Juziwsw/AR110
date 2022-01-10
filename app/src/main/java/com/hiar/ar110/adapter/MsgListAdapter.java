package com.hiar.ar110.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hiar.ar110.R;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.hileia.common.entity.proto.EntityOuterClass;

import java.util.ArrayList;
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/23 19:14
 */
public class MsgListAdapter extends RecyclerView.Adapter<MsgViewHolder> {
    public List<EntityOuterClass.Entity.RecentContactInfo> messageList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener onItemClickListener;
    private String userId = LeiaBoxEngine.getInstance().accountManager().getUserInfo().getUserID();

    public MsgListAdapter(Context context) {
        setHasStableIds(true);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MsgViewHolder(mLayoutInflater.inflate(R.layout.item_msg, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        int newPosition = holder.getAdapterPosition();
        if (newPosition < 0) return;
        EntityOuterClass.Entity.RecentContactInfo msgInfo = this.messageList.get(newPosition);
        if (msgInfo.getMsg() == null) {
            return;
        }
        holder.bindView(msgInfo, userId);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClick(EntityOuterClass.Entity.ContactInfo contact);
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
