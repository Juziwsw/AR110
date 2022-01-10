package com.hiar.ar110.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.hiar.ar110.R;
import com.hiar.ar110.data.Constants;
import com.hileia.common.entity.proto.EntityOuterClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * author: liwf
 * date: 2021/3/22 15:00
 */
public class ContactSelectAdapter extends RecyclerView.Adapter<ContactSelectViewHolder> {
    public List<EntityOuterClass.Entity.ContactInfo> contactList = new ArrayList<>();
    public List<EntityOuterClass.Entity.ContactInfo> selectedContact = new ArrayList<>();
    private List<String> disableContact = new ArrayList<>();
    private String mTheme = Constants.THEME_DARK;
    private LayoutInflater mLayoutInflater;
    private OnItemCheckedChangeListener onItemCheckChangeListener;

    public interface OnItemCheckedChangeListener {
        void onCheckedChanged(CompoundButton checkBox, EntityOuterClass.Entity.ContactInfo contact, boolean isChecked);
    }

    public ContactSelectAdapter(Context context) {
        setHasStableIds(true);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ContactSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactSelectViewHolder(mLayoutInflater.inflate(R.layout.item_select_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactSelectViewHolder holder, int position) {
        int newPos = holder.getAdapterPosition();
        if (newPos < 0) return;
        EntityOuterClass.Entity.ContactInfo contact = this.contactList.get(newPos);
        holder.bindView(contact, isEnable(contact), isChecked(contact), onItemCheckChangeListener, mTheme);
    }

    private boolean isEnable(EntityOuterClass.Entity.ContactInfo contact) {
        for (String userID : disableContact) {
            if (userID.equals(contact.getUserID())) {
                return false;
            }
        }
        return true;
    }

    private boolean isChecked(EntityOuterClass.Entity.ContactInfo contact) {
        for (EntityOuterClass.Entity.ContactInfo select : selectedContact) {
            if (select.getUserID().equals(contact.getUserID())) {
                return true;
            }
        }
        return false;
    }

    public void setTheme(String theme) {
        mTheme = theme;
    }

    @Override
    public void onViewRecycled(@NonNull ContactSelectViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemCheckChangeListener(OnItemCheckedChangeListener onItemCheckChangeListener) {
        this.onItemCheckChangeListener = onItemCheckChangeListener;
    }

    public boolean isCheckedAll() {
        for (EntityOuterClass.Entity.ContactInfo contact : contactList) {
            int position = -1;
            for (EntityOuterClass.Entity.ContactInfo info : selectedContact) {
                if (info.getUserID().equals(contact.getUserID())) {
                    position = selectedContact.indexOf(info);
                    break;
                }
            }
            int dPosition = -1;
            for (String info : disableContact) {
                if (info.equals(contact.getUserID())) {
                    dPosition = disableContact.indexOf(info);
                    break;
                }
            }
            if (position == -1 && dPosition == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = contactList.get(i).getLetter();
            if (sortStr != null) {
                if (!sortStr.isEmpty()) {
                    char firstChar = sortStr.toUpperCase(Locale.getDefault()).toCharArray()[0];
                    if ((int) firstChar == section) {
                        return i;
                    }
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }

}
