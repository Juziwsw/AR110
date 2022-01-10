package com.hiar.ar110.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hiar.ar110.R;
import com.hileia.common.entity.proto.EntityOuterClass;

import java.util.ArrayList;
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/23 9:44
 */
public class ContactSelectedAdapter extends RecyclerView.Adapter<ContactSelectedViewHolder> {
    public List<EntityOuterClass.Entity.ContactInfo> contactList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private final int count = 5;

    public ContactSelectedAdapter(Context context) {
        setHasStableIds(true);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ContactSelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactSelectedViewHolder(mLayoutInflater.inflate(R.layout.item_slected_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactSelectedViewHolder holder, int position) {
        int newPos = holder.getAdapterPosition();
        if (newPos < 0) return;
        if (contactList.size() > newPos) {
            holder.bindView(contactList.get(newPos), contactList.size(), newPos);
        } else {
            holder.bindView(null, contactList.size(), newPos);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ContactSelectedViewHolder holder) {
//        GlideUtil.ClearAvatar(glideRequest, holder.itemView.selected_contact_img_avatar)
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
