package com.hiar.ar110.adapter;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.hiar.ar110.R;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hiscene.imui.util.GlideUtils;
import com.hiscene.imui.widget.NiceImageView;

/**
 * author: liwf
 * date: 2021/3/23 9:45
 */
class ContactSelectedViewHolder extends RecyclerView.ViewHolder {
    private NiceImageView mSelectedContactAvatar;

    public ContactSelectedViewHolder(View itemView) {
        super(itemView);
        mSelectedContactAvatar = itemView.findViewById(R.id.selected_contact_img_avatar);
    }

    void bindView(EntityOuterClass.Entity.ContactInfo contact, int count, int position) {
        if (count > position) {
            mSelectedContactAvatar.setBorderColor(ActivityCompat.getColor(itemView.getContext(), R.color.avatar_border_color));
            if (contact.getHasPhoto()) {
                GlideUtils.loadAvatarNoCache(itemView.getContext().getApplicationContext(), mSelectedContactAvatar, contact.getAvatarUrl(), R.drawable.default_portrait);
            } else {
                mSelectedContactAvatar.setTextSeed(contact.getName());
            }
        } else {
            mSelectedContactAvatar.setBorderColor(ActivityCompat.getColor(itemView.getContext(), R.color.transparent));
            mSelectedContactAvatar.setImageResource(R.drawable.icon_selected_cover);
        }
    }
}
