package com.hiscene.imui.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

public class GlideUtils {

    public static void loadChatImage(final Context mContext, String imgUrl, final ImageView imageView) {

//        final RequestOptions options = new RequestOptions()
//                .placeholder(R.drawable.default_img_failed)// 正在加载中的图片
//                .error(R.drawable.default_img_failed); // 加载失败的图片
//
//        Glide.with(mContext).load(imgUrl).apply(options).into(new SimpleTarget<Drawable>() {
//            @Override
//            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
//                ImageSize imageSize = BitmapUtil.getImageSize(((BitmapDrawable) resource).getBitmap());
//                RelativeLayout.LayoutParams imageLP = (RelativeLayout.LayoutParams) (imageView.getLayoutParams());
//                imageLP.width = imageSize.getWidth();
//                imageLP.height = imageSize.getHeight();
//                imageView.setLayoutParams(imageLP);
//                imageView.setImageBitmap(((BitmapDrawable) resource).getBitmap());
//            }
//        });
        Glide.with(mContext).load(imgUrl).into(imageView);
    }

    /**
     * 加载头像
     *
     * @param imageView
     * @param url
     */
    public static void loadAvatarNoCache(final Context mContext, ImageView imageView, String url, int default_resId) {
        final WeakReference<ImageView> imageViewWeakReference = new WeakReference<>(imageView);
        ImageView target = imageViewWeakReference.get();
        if (target != null) {
            if (TextUtils.isEmpty(url)) {
                target.setImageResource(default_resId);
                return;
            }

            Glide.with(mContext).load(url)
                    .into(target);
        }
    }
}
