package com.hiar.ar110.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;

import com.hiar.ar110.R;
import com.hiscene.imui.widget.NiceImageView;

public abstract class LQRNineGridImageViewAdapter<T> {
    /**
     * 重写该方法，使用任意第三方图片加载工具加载图片
     */
    public abstract void onDisplayImage(Context context, NiceImageView imageView, T t);

    /**
     * 重写该方法自定义生成ImageView方式，用于九宫格头像中的一个个图片控件，可以设置ScaleType等属性
     */
    public NiceImageView generateImageView(Context context) {
        NiceImageView imageView = new NiceImageView(context);
        imageView.setTextColor(ContextCompat.getColor(context, R.color.avatar_text_color));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

}
