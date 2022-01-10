package com.hiar.ar110.adapter;

import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.hiar.ar110.R;
import com.hiar.ar110.data.Constants;
import com.hiar.ar110.widget.MediumBoldTextView;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Enums;
import com.hiscene.imui.util.GlideUtils;
import com.hiscene.imui.widget.NiceImageView;

/**
 * author: liwf
 * date: 2021/3/22 15:07
 */
public class ContactSelectViewHolder extends RecyclerView.ViewHolder {
    private ContactSelectAdapter.OnItemCheckedChangeListener onItemCheckChangeListener;

    private MediumBoldTextView mSelectContactName;
    private CheckBox mSelectContactCB;
    private ImageView mImgStatus;
    private View mDivideLine;
    private NiceImageView mImgAvatar;

    public ContactSelectViewHolder(View itemView) {
        super(itemView);
        mSelectContactName = itemView.findViewById(R.id.tv_select_contact_name);
        mSelectContactCB = itemView.findViewById(R.id.select_contact_cb);
        mDivideLine = itemView.findViewById(R.id.v_divide_line);
        mImgStatus = itemView.findViewById(R.id.img_status);
        mImgAvatar = itemView.findViewById(R.id.select_contact_img_avatar);
    }

    void bindView(EntityOuterClass.Entity.ContactInfo contact, boolean isEnable, boolean isChecked, ContactSelectAdapter.OnItemCheckedChangeListener onItemCheckedChangeListener, String theme) {

        this.onItemCheckChangeListener = onItemCheckedChangeListener;
        if (theme == Constants.THEME_LIGHT) {
            mSelectContactName.setTextColor(ContextCompat.getColor(mSelectContactName.getContext(), R.color.black_color));
            mSelectContactCB.setButtonDrawable(ContextCompat.getDrawable(mSelectContactCB.getContext(), R.drawable.check_box_selector));
            mDivideLine.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mDivideLine.getContext(), R.color.list_divider_light)));
        }

        mSelectContactName.setText(contact.getName());
        switch (contact.getDeviceType()) {
            case Enums.DeviceType.IOS_VALUE:
            case Enums.DeviceType.ANDROIDOS_VALUE:
                switch (contact.getOnlineStatus()) {
                    case 0:
//                        if (contact.offlineReason == Enums.OfflineReason.OFFLINE_REASON_EXIT_VALUE || contact.offlineReason == Enums.OfflineReason.OFFLINE_REASON_UNKONWN_VALUE) {
//                            mImgStatus.setImageResource(R.drawable.mobile_offline)
//                        } else {
//                            mImgStatus.setImageResource(R.drawable.device_push)
//                        }
                        break;
                    case 1:
                        mImgStatus.setImageResource(R.drawable.mobile_online);
                        break;
                    case 2:
                        mImgStatus.setImageResource(R.drawable.device_talking);
                        break;
                    case 3:
                        mImgStatus.setImageResource(R.drawable.device_live);
                        break;
                    default:
                        mImgStatus.setImageResource(R.color.transparent);
                }
                break;
            case Enums.DeviceType.GLASSES_VALUE:
                switch (contact.getOnlineStatus()) {
                    case 0:
                        mImgStatus.setImageResource(R.drawable.glasses_offline);
                        break;
                    case 1:
                        mImgStatus.setImageResource(R.drawable.glasses_online);
                        break;
                    case 2:
                        mImgStatus.setImageResource(R.drawable.device_talking);
                        break;
                    case 3:
                        mImgStatus.setImageResource(R.drawable.device_live);
                        break;
                    default:
                        mImgStatus.setImageResource(R.color.transparent);
                        break;
                }
                break;
            case Enums.DeviceType.WINDOWS_VALUE:
                switch (contact.getOnlineStatus()) {
                    case 0:
                        mImgStatus.setImageResource(R.drawable.windows_offline);
                        break;
                    case 1:
                        mImgStatus.setImageResource(R.drawable.windows_online);
                        break;
                    case 2:
                        mImgStatus.setImageResource(R.drawable.device_talking);
                        break;
                    case 3:
                        mImgStatus.setImageResource(R.drawable.device_live);
                        break;
                    default:
                        mImgStatus.setImageResource(R.color.transparent);
                        break;
                }
                break;
            default:
                mImgStatus.setImageResource(R.color.transparent);
                break;
        }

        if (contact.getHasPhoto()) {
            GlideUtils.loadAvatarNoCache(itemView.getContext().getApplicationContext(), mImgAvatar, contact.getAvatarUrl(), R.drawable.default_portrait);
        } else {
            mImgAvatar.setTextSeed(contact.getName());
        }
        mSelectContactCB.setEnabled(isEnable);
        mSelectContactCB.setChecked(isChecked);

        itemView.setOnClickListener(v -> {
            if (isEnable) {
                mSelectContactCB.setChecked(!mSelectContactCB.isChecked());
                onItemCheckChangeListener.onCheckedChanged(mSelectContactCB, contact, mSelectContactCB.isChecked());
            }
        });
    }
}
