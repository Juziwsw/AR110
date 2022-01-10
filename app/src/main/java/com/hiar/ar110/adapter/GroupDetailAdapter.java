package com.hiar.ar110.adapter;

import android.content.Context;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hiar.ar110.R;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hiscene.imui.util.GlideUtils;
import com.hiscene.imui.widget.NiceImageView;

/**
 * author: liwf
 * date: 2021/3/19 13:44
 */
public class GroupDetailAdapter extends BaseQuickAdapter<EntityOuterClass.Entity.ContactInfo, BaseViewHolder> {
    private Context mContext;

    public GroupDetailAdapter(int layoutResId, Context context) {
        super(layoutResId);
        setHasStableIds(true);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder holder, EntityOuterClass.Entity.ContactInfo item) {
        if (item.getAvatarUrl().isEmpty()) {
            ((NiceImageView) holder.itemView.findViewById(R.id.img_avatar)).setTextSeed(item.getName());
        } else {
            GlideUtils.loadAvatarNoCache(mContext, (holder.itemView.findViewById(R.id.img_avatar)), item.getAvatarUrl(), R.drawable.default_img_failed);
        }
        ((TextView) holder.itemView.findViewById(R.id.tv_name)).setText(item.getName());
    }
}
