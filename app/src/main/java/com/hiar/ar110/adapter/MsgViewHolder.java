package com.hiar.ar110.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.widget.LQRNineGridImageView;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Enums;
import com.hileia.common.manager.ChatManager;
import com.hiscene.imui.util.GlideUtils;
import com.hiscene.imui.util.TimeUtils;
import com.hiscene.imui.widget.NiceImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/23 16:33
 */
public class MsgViewHolder extends RecyclerView.ViewHolder {
    private TextView tvTime, tvLastMsg, tvContactName;
    private LQRNineGridImageView imgAvatar;

    public MsgViewHolder(View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.tv_time);
        tvLastMsg = itemView.findViewById(R.id.tv_last_msg);
        tvContactName = itemView.findViewById(R.id.tv_contact_name);
        imgAvatar = itemView.findViewById(R.id.img_avatar);
    }

    void bindView(EntityOuterClass.Entity.RecentContactInfo recentContactInfo, String selfUserId) {
        String text = TimeUtils.getTimeStringAutoShort(tvTime.getContext(), new Date(recentContactInfo.getMsg().getTimestamp()), false);
        tvTime.setText(text);
        if (recentContactInfo.getMsg().getType() == ChatManager.ChatMsgType.CHAT_MSG_TYPE_CALL.ordinal()) {
            switch (recentContactInfo.getMsg().getCallDealType()) {
                case Enums.CallDealType.UnAnswered_VALUE:
                    if (recentContactInfo.getMsg().getCallerId().equals(selfUserId)) {
                        text = "[" + itemView.getContext().getString(R.string.label_call_other_unanswered) + "]";
                    } else {
                        text = "[" + itemView.getContext().getString(R.string.label_call_unanswered) + "]";
                    }
                    tvLastMsg.setText(text);
                    break;
                case Enums.CallDealType.Answered_VALUE:
                    text = "[" + String.format(itemView.getContext().getString(R.string.label_call_time), TimeUtils.getTimeString((int) recentContactInfo.getMsg().getCallTime())) + "]";
                    tvLastMsg.setText(text);
                    break;
                case Enums.CallDealType.Reject_VALUE:
                    if (recentContactInfo.getMsg().getCallerId().equals(selfUserId)) {
                        text = "[" + itemView.getContext().getString(R.string.label_call_other_rejected) + "]";
                    } else {
                        text = "[" + itemView.getContext().getString(R.string.label_call_rejected) + "]";
                    }
                    tvLastMsg.setText(text);
                    break;
                case Enums.CallDealType.Canceled_VALUE:
                    if (recentContactInfo.getMsg().getCallerId().equals(selfUserId)) {
                        text = "[" + itemView.getContext().getString(R.string.label_call_cancelled) + "]";
                    } else {
                        text = "[" + itemView.getContext().getString(R.string.label_call_other_cancelled) + "]";
                    }
                    tvLastMsg.setText(text);
                    break;
            }
        } else {
            tvLastMsg.setText(recentContactInfo.getMsg().getText());
        }
        List<EntityOuterClass.Entity.ContactInfo> contacts = new ArrayList<>();
        if (recentContactInfo.getRctype() == Enums.RecentContactType.PEOPLE_TYPE_VALUE) {
            EntityOuterClass.Entity.ContactInfo contactInfo = LeiaBoxEngine.getInstance().contactManager().getContactInfo(recentContactInfo.getRcid());
            contacts.add(contactInfo);
            tvContactName.setText(contactInfo.getName());
        } else if (recentContactInfo.getRctype() == Enums.RecentContactType.GROUP_TYPE_VALUE) {
            EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(recentContactInfo.getRcid());
            tvContactName.setText(groupInfo.getName());
            int size = groupInfo.getMemberCount();
            for (int i = 0; i < size; i++) {
                EntityOuterClass.Entity.GroupMemberInfo memberInfo = groupInfo.getMember(i);
                EntityOuterClass.Entity.ContactInfo.Builder contactInfo = EntityOuterClass.Entity.ContactInfo.newBuilder();
                if (memberInfo.getUserID().equals(LeiaBoxEngine.getInstance().accountManager().getUserInfo().getUserID())) {
                    EntityOuterClass.Entity.UserInfo userInfo = LeiaBoxEngine.getInstance().accountManager().getUserInfo();
                    contactInfo.setAvatarUrl(userInfo.getAvatarUrl());
                    contactInfo.setName(userInfo.getName());
                    contactInfo.setHasPhoto(userInfo.getHasPhoto());
                } else {
                    EntityOuterClass.Entity.ContactInfo contact = LeiaBoxEngine.getInstance().contactManager().getContactInfo(memberInfo.getUserID());
                    contactInfo.setAvatarUrl(contact.getAvatarUrl());
                    contactInfo.setName(contact.getName());
                    contactInfo.setHasPhoto(contact.getHasPhoto());
                }
                contacts.add(contactInfo.build());
            }
        }
        imgAvatar.setAdapter(adapter);
        imgAvatar.setImagesData(contacts);
        if (contacts.size() == 1) {
            imgAvatar.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent));
        } else {
            imgAvatar.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.group_avatar_bg));
        }
    }

    LQRNineGridImageViewAdapter<EntityOuterClass.Entity.ContactInfo> adapter = new LQRNineGridImageViewAdapter<EntityOuterClass.Entity.ContactInfo>() {
        @Override
        public void onDisplayImage(Context context, NiceImageView imageView, EntityOuterClass.Entity.ContactInfo contact) {
            if (contact.getHasPhoto()) {
                String url = contact.getAvatarUrl();
                int index = url.indexOf("?");
                if (index != -1) {
                    url = url.substring(0, index);
                }
                if (imageView.getDrawable() == null) {
                    imageView.setImageResource(R.drawable.default_portrait);
                }
                GlideUtils.loadAvatarNoCache(itemView.getContext(), imageView, url, R.drawable.default_portrait);
            } else {
                imageView.setTextSeed(contact.getName());
            }
        }
    };
}
