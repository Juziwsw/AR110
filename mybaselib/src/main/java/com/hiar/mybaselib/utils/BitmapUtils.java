package com.hiar.mybaselib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.hileia.common.utils.XLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author xujiangang
 * @date 2017/11/2
 */

public final class BitmapUtils {

    // 日志 TAG
    private static final String TAG = com.hiar.mybaselib.utils.BitmapUtils.class.getSimpleName();

    /**
     * 将十进制颜色(Int)转换为Drawable对象
     *
     * @param color
     * @return
     */
    public static Drawable intToDrawable(final int color) {
        try {
            return new ColorDrawable(color);
        } catch (Exception e) {
            XLog.e(TAG, "intToDrawable: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 将十六进制颜色(String)转化为Drawable对象
     *
     * @param color
     * @return
     */
    public static Drawable stringToDrawable(final String color) {
        if (TextUtils.isEmpty(color)) return null;
        try {
            return new ColorDrawable(Color.parseColor(color));
        } catch (Exception e) {
            XLog.e(TAG, "stringToDrawable: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 图片着色
     *
     * @param drawable
     * @param tintColor
     * @return
     */
    public static Drawable tintIcon(final Drawable drawable, @ColorInt final int tintColor) {
        if (drawable != null) {
            try {
                drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            } catch (Exception e) {
                XLog.e(TAG, "tintIcon: %s", e.getLocalizedMessage());
            }
        }
        return drawable;
    }

    /**
     * .9 图片着色
     *
     * @param context   {@link Context}
     * @param tintColor
     * @param id
     * @return
     */
    public static Drawable tint9PatchDrawableFrame(final Context context, @ColorInt final int tintColor, @DrawableRes final int id) {
        if (context == null) return null;
        try {
            final NinePatchDrawable toastDrawable = (NinePatchDrawable) getDrawable(context, id);
            return tintIcon(toastDrawable, tintColor);
        } catch (Exception e) {
            XLog.e(TAG, "tint9PatchDrawableFrame: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 设置背景
     *
     * @param view
     * @param drawable
     */
    public static void setBackground(@NonNull final View view, final Drawable drawable) {
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                view.setBackground(drawable);
            else
                view.setBackgroundDrawable(drawable);
        }
    }

    /**
     * 获取 Drawable
     *
     * @param context {@link Context}
     * @param id
     * @return
     */
    public static Drawable getDrawable(final Context context, @DrawableRes final int id) {
        if (context == null) return null;
        return ActivityCompat.getDrawable(context, id);
    }

    /**
     * 通过Resources获取Bitmap
     *
     * @param context {@link Context}
     * @param resId
     * @return
     */
    public static Bitmap getBitmapFromResources(final Context context, final int resId) {
        if (context == null) return null;
        try {
            return BitmapFactory.decodeResource(context.getResources(), resId);
        } catch (Exception e) {
            XLog.e(TAG, "getBitmapFromResources: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 通过Resources获取Drawable
     *
     * @param context {@link Context}
     * @param resId
     * @return
     */
    public static Drawable getDrawableFromResources(final Context context, final int resId) {
        if (context == null) return null;
        try {
            return context.getResources().getDrawable(resId);
        } catch (Exception e) {
            XLog.e(TAG, "getDrawableFromResources: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 获取本地SDCard 图片
     *
     * @param filePath 文件路径
     * @return
     */
    public static Bitmap getSDCardBitmapStream(final String filePath) {
        try {
            FileInputStream fis = new FileInputStream(new File(filePath)); // 文件输入流
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            return bmp;
        } catch (Exception e) {
            XLog.e(TAG, "getSDCardBitmapStream: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * view 转 bitmap
     */
    public static Bitmap getViewBitmap(View view) {
        XLog.i(TAG, "getViewBitmap");
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 由file转bitmap
     */
    public static Bitmap decodeBitmapFromFilePath(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//如此，无法decode bitmap
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;//如此，方可decode bitmap

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 获取本地SDCard 图片
     *
     * @param filePath 文件路径
     * @return
     */
    public static Bitmap getSDCardBitmapFile(final String filePath) {
        try {
            return BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            XLog.e(TAG, "getSDCardBitmapFile: %s", e.getLocalizedMessage());
        }
        return null;
    }

    public static Bitmap getAssetsBitmapFile(Context context, final String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(filePath);
        } catch (IOException e) {
            XLog.i(TAG, e.toString());
        }
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * 获取Bitmap
     *
     * @param is
     * @return
     */
    public static Bitmap getBitmap(final InputStream is) {
        if (is == null) return null;
        try {
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            XLog.e(TAG, "getBitmap: %s", e.getLocalizedMessage());
        }
        return null;
    }

    // =

    /**
     * Bitmay 转换成 byte[]
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToByte(final Bitmap bitmap) {
        return bitmapToByte(bitmap, 100, Bitmap.CompressFormat.PNG);
    }

    /**
     * Bitmay 转换成 byte[]
     *
     * @param bitmap
     * @param format
     * @return
     */
    public static byte[] bitmapToByte(final Bitmap bitmap, final Bitmap.CompressFormat format) {
        return bitmapToByte(bitmap, 100, format);
    }

    /**
     * Bitmay 转换成 byte[]
     *
     * @param bitmap
     * @param quality
     * @param format
     * @return
     */
    public static byte[] bitmapToByte(final Bitmap bitmap, final int quality, final Bitmap.CompressFormat format) {
        if (bitmap == null || format == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(format, quality, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            XLog.e(TAG, "bitmapToByte: %s", e.getLocalizedMessage());
        }
        return null;
    }

    // =

    /**
     * Drawable 转换成 byte[]
     *
     * @param drawable
     * @return
     */
    public static byte[] drawableToByte(final Drawable drawable) {
        return bitmapToByte(drawableToBitmap(drawable));
    }

    /**
     * Drawable 转换成 byte[]
     *
     * @param drawable
     * @param format
     * @return
     */
    public static byte[] drawableToByte(final Drawable drawable, final Bitmap.CompressFormat format) {
        return bitmapToByte(drawableToBitmap(drawable), format);
    }

    /**
     * Drawable 转换成 byte[]
     *
     * @param drawable
     * @return
     */
    public static byte[] drawableToByte2(final Drawable drawable) {
        return drawable == null ? null : bitmapToByte(drawableToBitmap2(drawable));
    }

    /**
     * Drawable 转换成 byte[]
     *
     * @param drawable
     * @param format
     * @return
     */
    public static byte[] drawableToByte2(final Drawable drawable, final Bitmap.CompressFormat format) {
        return drawable == null ? null : bitmapToByte(drawableToBitmap2(drawable), format);
    }

    // =

    /**
     * byte 数组转换为Bitmap
     *
     * @param bytes
     * @return
     */
    public static Bitmap byteToBitmap(final byte[] bytes) {
        try {
            return (bytes == null || bytes.length == 0) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            XLog.e(TAG, "byteToBitmap: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Byte[] 转换成 Drawable
     *
     * @param bytes
     * @return
     */
    public static Drawable byteToDrawable(Context context, final byte[] bytes) {
        return bitmapToDrawable(context, byteToBitmap(bytes));
    }

    /**
     * Bitmap 转换成 Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Context context, final Bitmap bitmap) {
        try {
            return bitmap == null ? null : new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            XLog.e(TAG, "bitmapToDrawable: %s", e.getLocalizedMessage());
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    /**
     * Drawable 转换成 Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(final Drawable drawable) {
        try {
            return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
        } catch (Exception e) {
            XLog.e(TAG, "drawableToBitmap: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Drawable 转换 Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap2(final Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Drawable 转换 Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap3(final Drawable drawable) {
        if (drawable == null) return null;
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable {@link Drawable}
     * @return {@link Bitmap}
     */
    public static Bitmap drawableToBitmap4(final Drawable drawable) {
        if (drawable == null) return null;
        try {
            // 取 drawable 的长宽
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            // 取 drawable 的颜色格式
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            // 建立对应 bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            // 建立对应 bitmap 的画布
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            // 把 drawable 内容画到画布中
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            XLog.e(TAG, "drawableToBitmap4: %s", e.getLocalizedMessage());
        }
        return null;
    }

    // =

    /**
     * 保存图片到SD卡 - JPEG
     *
     * @param bitmap  需要保存的数据
     * @param path    保存路径
     * @param quality 压缩比例
     * @return {@link Bitmap}
     */
    public static boolean saveBitmapToSDCardJPEG(final Bitmap bitmap, final String path, final int quality) {
        return saveBitmapToSDCard(bitmap, path, Bitmap.CompressFormat.JPEG, quality);
    }

    /**
     * 保存图片到SD卡 - PNG
     *
     * @param bitmap 需要保存的数据
     * @param path   保存路径
     * @return {@link Bitmap}
     */
    public static boolean saveBitmapToSDCardPNG(final Bitmap bitmap, final String path) {
        return saveBitmapToSDCard(bitmap, path, Bitmap.CompressFormat.PNG, 80);
    }

    /**
     * 保存图片到SD卡 - PNG
     *
     * @param bitmap  需要保存的数据
     * @param path    保存路径
     * @param quality 压缩比例
     * @return {@link Bitmap}
     */
    public static boolean saveBitmapToSDCardPNG(final Bitmap bitmap, final String path, final int quality) {
        return saveBitmapToSDCard(bitmap, path, Bitmap.CompressFormat.PNG, quality);
    }

    /**
     * 保存图片到SD卡 - PNG
     *
     * @param bitmap  需要保存的数据
     * @param path    保存路径
     * @param quality 压缩比例
     * @return {@link Bitmap}
     */
    public static boolean saveBitmapToSDCard(final Bitmap bitmap, final String path, final int quality) {
        return saveBitmapToSDCard(bitmap, path, Bitmap.CompressFormat.PNG, quality);
    }

    /**
     * 保存图片到SD卡
     *
     * @param bitmap   图片资源
     * @param filePath 保存路径
     * @param format   如 Bitmap.CompressFormat.PNG
     * @param quality  保存的图片质量, 100 则完整质量不压缩保存
     * @return 保存结果
     */
    public static boolean saveBitmapToSDCard(final Bitmap bitmap, final String filePath, final Bitmap.CompressFormat format, final int quality) {
        XLog.i(TAG, "saveBitmapToSDCard path: %s", filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            if (fos != null) {
                bitmap.compress(format, quality, fos);
                fos.close();
            }
        } catch (Exception e) {
            XLog.e(TAG, "saveBitmapToSDCard: %s", e.getLocalizedMessage());
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    // =

    /**
     * 将Drawable转化为Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap getBitmapFromDrawable(final Drawable drawable) {
        try {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            XLog.e(TAG, "getBitmapFromDrawable: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 通过View, 获取背景转换Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap bitmapToViewBackGround(final View view) {
        if (view == null) return null;
        try {
            Bitmap ret = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(ret);
            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null) {
                bgDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            view.draw(canvas);
            return ret;
        } catch (Exception e) {
            XLog.e(TAG, "bitmapToViewBackGround: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 通过 View 获取 Bitmap, 绘制整个View
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(final View view) {
        if (view == null) return null;
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            XLog.e(TAG, "getBitmapFromView: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 把一个View的对象转换成bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView2(final View view) {
        if (view == null) return null;
        try {
            view.clearFocus();
            view.setPressed(false);
            // 能画缓存就返回 false
            boolean willNotCache = view.willNotCacheDrawing();
            view.setWillNotCacheDrawing(false);
            int color = view.getDrawingCacheBackgroundColor();
            view.setDrawingCacheBackgroundColor(0);
            if (color != 0) {
                view.destroyDrawingCache();
            }
            view.buildDrawingCache();
            Bitmap cacheBitmap = view.getDrawingCache();
            if (cacheBitmap == null) {
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
            // Restore the view
            view.destroyDrawingCache();
            view.setWillNotCacheDrawing(willNotCache);
            view.setDrawingCacheBackgroundColor(color);
            return bitmap;
        } catch (Exception e) {
            XLog.e(TAG, "getBitmapFromView2: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static int caculateInSampleSize(final BitmapFactory.Options options, final int targetWidth, final int targetHeight) {
        if (options == null) return 0;

        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > targetWidth || height > targetHeight) {
            int widthRadio = Math.round(width * 1.0f / targetWidth);
            int heightRadio = Math.round(height * 1.0f / targetHeight);
            inSampleSize = Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    // = ImageView 相关 =

    /**
     * 根据ImageView获适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    public static int[] getImageViewSize(final ImageView imageView) {
        int[] imgSize = new int[]{0, 0};
        if (imageView == null) return imgSize;
        // =
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        // 获取 imageView 的实际宽度
        int width = imageView.getWidth();
        if (width <= 0) {
            width = lp.width; // 获取 imageView 在layout中声明的宽度
        }
        if (width <= 0) {
            // width = imageView.getMaxWidth(); // 检查最大值
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }
        // =
        // 获取 imageView 的实际高度
        int height = imageView.getHeight();
        if (height <= 0) {
            height = lp.height; // 获取 imageView 在layout中声明的宽度
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // 检查最大值
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        // =
        imgSize[0] = width;
        imgSize[1] = height;
        return imgSize;
    }

    /**
     * 通过反射获取 imageView 的某个属性值
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(final Object object, final String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            XLog.e(TAG, "getImageViewFieldValue: %s", e.getLocalizedMessage());
        }
        return value;
    }

    /**
     * 获取图片宽度高度(不加载解析图片)
     *
     * @param filePath
     * @return
     */
    public static int[] getImageWidthHeight(final String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);
        if (file.isDirectory() || !file.exists()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不解析图片信息
        options.inJustDecodeBounds = true;
        // 此时返回的bitmap为 null
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        // options.outHeight为原始图片的高
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 计算缩放比
     *
     * @param bitmapOptions option
     * @param reqWidth      当前图片宽度
     * @param reqHeight     当前图片高度
     * @return int 缩放比
     * @version V1.0.0
     */
    public static int calculateInSampleSize(BitmapFactory.Options bitmapOptions, int reqWidth, int reqHeight) {
        final int height = bitmapOptions.outHeight;
        final int width = bitmapOptions.outWidth;
        int sampleSize = 1;
        XLog.i(TAG, "width- %d height- %d reqWidth- %d reqHeight- %d", width, height, reqWidth, reqHeight);
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            XLog.i(TAG, "widthRatio- %d heightRatio- %d", widthRatio, heightRatio);
            sampleSize = Math.max(heightRatio, widthRatio);
        }
        return sampleSize;
    }

    /**
     * 通过文件路径读获取Bitmap防止OOM以及解决图片旋转问题
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapFromFile(String filePath) {
        /** Decode image size */
        BitmapFactory.Options option = new BitmapFactory.Options();
        /** 只取宽高防止oom */
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, option);

        int scale = calculateInSampleSize(option, 1280, 960);

        BitmapFactory.Options options = new BitmapFactory.Options();
        /** Decode with inSampleSize，比直接算出options中的使用更少的内存*/
        options.inSampleSize = scale;
        /** 内存不足的时候可被擦除 */
        options.inPurgeable = true;
        /** 深拷贝 */
        options.inInputShareable = true;

        Bitmap result = BitmapFactory.decodeFile(filePath, options);
        return result;
    }

    public static Bitmap decodeResource(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;//获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private static int getResizedDimension(int maxPrimary, int maxSecondary,
                                           int actualPrimary, int actualSecondary) {
        // If no dominant value at all, just return the actual.
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling
        // ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    private static int findBestSampleSize(int actualWidth, int actualHeight,
                                          int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    public static Bitmap getImageFromData(byte[] data, int mMaxWidth,
                                          int mMaxHeight) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't
            // support it?
            // decodeOptions.inPreferQualityOverSpeed =
            // PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
                    actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null
                    && (tempBitmap.getWidth() > desiredWidth || tempBitmap
                    .getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
                        desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }
        return bitmap;
    }

    public static Bitmap getImageFromBitmap(Bitmap srcBitmap, int mMaxWidth,
                                            int mMaxHeight) {
        Bitmap bitmap;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            bitmap = srcBitmap;
        } else {
            int actualWidth = srcBitmap.getWidth();
            int actualHeight = srcBitmap.getHeight();

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth);
            bitmap = Bitmap.createScaledBitmap(srcBitmap, desiredWidth,
                    desiredHeight, true);
        }
        return bitmap;
    }

    public static Bitmap getImageFromFile(File file, int mMaxWidth,
                                          int mMaxHeight) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        Bitmap.Config preferredConfig = Bitmap.Config.RGB_565;
        try {
            if (mMaxWidth == 0 && mMaxHeight == 0) {
                bitmap = BitmapFactory.decodeFile(file.getPath());
            } else {
                // If we have to resize this image, first get the natural
                // bounds.
                decodeOptions.inJustDecodeBounds = true;
                decodeOptions.inPreferredConfig = preferredConfig;
                bitmap = BitmapFactory
                        .decodeFile(file.getPath(), decodeOptions);
                int actualWidth = decodeOptions.outWidth;
                int actualHeight = decodeOptions.outHeight;

                // Then compute the dimensions we would ideally like to decode
                // to.
                int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                        actualWidth, actualHeight);
                int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                        actualHeight, actualWidth);

                // Decode to the nearest power of two scaling factor.
                decodeOptions.inJustDecodeBounds = false;
                // TODO(ficus): Do we need this or is it okay since API 8
                // doesn't
                // support it?
                // decodeOptions.inPreferQualityOverSpeed =
                // PREFER_QUALITY_OVER_SPEED;
                decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
                        actualHeight, desiredWidth, desiredHeight);
                decodeOptions.inPreferredConfig = preferredConfig;
                Bitmap tempBitmap = BitmapFactory.decodeFile(file.getPath(),
                        decodeOptions);
                // If necessary, scale down to the maximal acceptable size.
                if (tempBitmap != null
                        && (tempBitmap.getWidth() > desiredWidth || tempBitmap
                        .getHeight() > desiredHeight)) {
                    bitmap = Bitmap.createScaledBitmap(tempBitmap,
                            desiredWidth, desiredHeight, true);
                    tempBitmap.recycle();
                } else {
                    bitmap = tempBitmap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap createBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = ActivityCompat.getDrawable(context, vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    /**
     * 根据资源 Id，加载出一个 View。并且保留原始布局的 layout 属性
     * 注意，使用 LayoutInflater 的 inflate 方法加载 xml 布局文件时，如果第二个参数（即 parent root）为null，则会丢失所有 layout 开头的属性（如 layout_width等）
     */
    public static View loadView(Context context, int viewResId) {
        if (context == null || viewResId <= 0) return null;
        FrameLayout frameLayout = new FrameLayout(context);
        View view = LayoutInflater.from(context).inflate(viewResId, frameLayout, false);
        return view;
    }

    /**
     * 从静态资源中，渲染并导出一张Bitmap图片
     *
     * @param context
     * @param viewResId
     * @return
     */
    public static Bitmap loadBitmap(Context context, int viewResId) {
        if (context == null || viewResId <= 0) return null;
        return loadBitmap(loadView(context, viewResId));

    }

    /**
     * 从一个动态 View 中，渲染并导出一张Bitmap图片
     */
    public static Bitmap loadBitmap(View view) {
        if (view == null) return null;
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * Bitmap 缩放
     * */
    public static Bitmap getScaledBitmap(Bitmap src, float scaleWidth, float scaleHeight) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        src.recycle();
        return dst;
    }
}
