package com.example.mediaengine.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import com.hileia.common.utils.XLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author xujiangang
 * @date 15/03/2019
 * Email: jiangang.xu@hiscene.com
 */

public class Luban {
    private static final String TAG = "Luban";
    private static final String DEFAULT_DISK_CACHE_DIR = "temp";

    private String mTargetDir;
    private Luban(Builder builder) {}

    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context
     *     A context.
     */
    private File createImageFile(Context context, String suffix) {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageDir(context).getAbsolutePath();
        }

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String cacheBuilder = mTargetDir + "/" +
                timeStamp + (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);

        return new File(cacheBuilder);
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context
     *     A context.
     *
     * @see #getImageCacheDir(Context, String)
     */
    @Nullable
    private File getImageDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context
     *     A context.
     * @param cacheName
     *     The name of the subdirectory in which to store the cache.
     *
     * @see #getImageDir(Context)
     */
    @Nullable
    private File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result;
        }
        XLog.e(TAG, "default disk cache dir is null");
        return null;
    }

    private void deleteFilesByDirectory(Context context) {
        File directory = getImageDir(context);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    /**
     * start compress and return the mFile
     */
    @WorkerThread
    private File get(String path, Context context) throws IOException {
        return new CompressEngine(path, createImageFile(context, Checker.checkSuffix(path))).compress();
    }

    public static class Builder {

        private Context context;

        Builder(Context context) {
            this.context = context;
        }

        private Luban build() {
            return new Luban(this);
        }

        public File get(String path) throws IOException {
            return build().get(path, context);
        }
        public void delete() {
            build().deleteFilesByDirectory(context);
        }
    }

}
