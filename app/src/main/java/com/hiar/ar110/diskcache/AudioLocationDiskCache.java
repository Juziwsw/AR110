package com.hiar.ar110.diskcache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.hiar.ar110.data.MediaLocationData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class AudioLocationDiskCache {
    public static final String TAG = AudioLocationDiskCache.class.getSimpleName();
    private DiskLruCache mDiskLruCache;
    private int mDiskCacheSize = 1024 * 1024;
    private File audioCacheDir = null;
    private Context mContext = null;
    private static AudioLocationDiskCache mInstance;

    public static AudioLocationDiskCache getInstance(Context context) {
        if(null == mInstance) {
            mInstance = new AudioLocationDiskCache(context);
        }
        return mInstance;
    }


    private void  createDiskCache() {
        try {
            File cacheDir = mContext.getCacheDir();
            audioCacheDir = new File(cacheDir + "/audiolocation/location");
            Log.d(TAG, "cacheDir=" + cacheDir);
            //如果文件夹不存在则创建
            if (!audioCacheDir.exists()) {
                audioCacheDir.mkdirs();
            }

            //第一个参数指定的是数据的缓存地址，第二个参数指定当前应用程序的版本号，
            //第三个参数指定同一个key可以对应多少个缓存文件，基本都是传1，第四个参数指定最多可以缓存多少字节的数据
            mDiskLruCache = DiskLruCache.open(audioCacheDir, getAppVersion(mContext), 1, mDiskCacheSize);
        } catch (IOException e) {
            Log.i(TAG, "fail to open cache");
        }
    }


    //初始化函数
    AudioLocationDiskCache(Context context) {
        mContext = context;
        createDiskCache();
    }

    //获取应用的版本号
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    //添加文件经纬度信息到缓存
    public void addAudioLocation(final MediaLocationData info) {
        if(info == null) {
            return;
        }

        String name = info.fileName;
        if (name == null) {
            return;
        }

        if (mDiskLruCache != null) {
            final String key = hashKeyForDisk(name);
            OutputStream out = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot == null) {
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(0);
                        Gson gson = new Gson();
                        String jstr = gson.toJson(info);
                        out.write(jstr.getBytes());
                        editor.commit();
                        out.close();
                        mDiskLruCache.flush();
                    }
                } else {
                    snapshot.getInputStream(0).close();
                }
            } catch (final IOException e) {

            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (final IOException e1) {
                    e1.printStackTrace();
                } catch (final IllegalStateException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public boolean removeAudioLoc(final String fileName) {
        MediaLocationData info = null;

        if (fileName == null) {
            return false;
        }

        final String key = hashKeyForDisk(fileName);//md5生成key
        if (mDiskLruCache != null) {
            try {
                return mDiskLruCache.remove(key);
            } catch (final IOException e) {

            } finally {

            }
        }
        return false;
    }


    //通过车牌号码从缓存中取出车辆基本信息
    public final MediaLocationData getAudioLocFromDiskCache(final String fileName) {
        MediaLocationData info = null;

        if (fileName == null) {
            return null;
        }

        final String key = hashKeyForDisk(fileName);//md5生成key
        if (mDiskLruCache != null) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(0);
                    if (inputStream != null) {
                        int len = inputStream.available();
                        if(len > 0) {
                            byte[] buf = new byte[len];
                            inputStream.read(buf);
                            String jstr = new String(buf,"UTF-8");
                            Gson gson = new Gson();
                            info = gson.fromJson(jstr, MediaLocationData.class);
                        }
                    }
                }
            } catch (final IOException e) {

            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                }
            }
        }
        return info;
    }


    //将key进行MD5编码
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public void onDestroy() {
        if(null != mDiskLruCache) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDiskLruCache = null;
        }
    }

    public void deleteAll() {
        if(mDiskLruCache != null && audioCacheDir != null) {
            if(audioCacheDir.exists()) {
                try {
                    DiskLruCache.deleteContents(audioCacheDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                audioCacheDir = null;
            }
        }

        createDiskCache();
    }
}
