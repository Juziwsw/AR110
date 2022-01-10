package com.hiar.mybaselib.utils;

import android.content.res.AssetManager;

import com.hileia.common.utils.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtil {
    // 日志 TAG
    private static final String TAG = com.hiar.mybaselib.utils.FileUtil.class.getSimpleName();

    public static boolean CopyAssets2Sdcard(InputStream in, String path) {
        try {
            OutputStream out = new FileOutputStream(new File(path));
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void CopyAssetsFile2Sdcard(AssetManager assetManager, String assetsFile, String path, boolean needRefresh) throws IOException {
        InputStream inputStream = assetManager.open(assetsFile);
        File outFile = new File(path);
        if (outFile.exists() && !needRefresh) {
            return;
        }
        OutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
        inputStream.close();
        out.close();
    }

    public static void CopyAssetsFolder2Sdcard(AssetManager assetManager, String assetsFolder, String path, boolean needRefresh) {
        try {
            File outDir = new File(path);
            if (!outDir.exists()) {
                outDir.mkdir();
            }

            String[] list = assetManager.list(assetsFolder);
            for (String name : list) {
                if (!name.contains(".")) {
                    //dir
                    CopyAssetsFolder2Sdcard(assetManager, assetsFolder + File.separator + name, path + File.separator + name, needRefresh);
                } else {
                    //file
                    CopyAssetsFile2Sdcard(assetManager, assetsFolder + File.separator + name, path + File.separator + name, needRefresh);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveStringToSDCard(String res, String path) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path));
            outputStream.write(res.getBytes());
            outputStream.close();
        } catch (Exception e) {
            XLog.i(TAG, "saveStringToSDCard exception: " + e.toString() + " path: " + path + " res: " + res);
            e.printStackTrace();
        }
    }

    public static String getStringFromSDCard(String path) {
        File file = new File(path);
        byte[] data = null;
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (Exception e) {
            XLog.i(TAG, "getStringFromSDCard exception: " + e.toString() + " path: " + path);
            e.printStackTrace();
        }
        if (data != null) {
            return new String(data);
        } else {
            return null;
        }
    }

}
