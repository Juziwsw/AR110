package com.example.mediaengine;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class Constant {
    public static int STATUS_FAIL = 0; //失败
    public static int STATUS_SUCCESS = 1;  //成功
    public static int STATUS_RECYCLE = -1;  //成功

    public static int IM_UNCONNECT_STATE = 100;
    public static int IM_CONNECTING_STATE = 101;
    public static int IM_CONNECTED_STATE = 102;
    public static int IM_CONNECT_FAILED_STATE = 103;
    public static int IM_CONNECT_LOST_STATE = 104;
    public static int IM_HAS_KICKOUT_STATE = 105;
    public static int IM_TOKEN_EXPIRED_STATE = 106;
    public static int IM_LOGOUT_STATE = 107;

    public static String PICTURES_DIRECTORY = "HiLeia";
    public static String TEMP_DIRECTORY = "temp";

    public static int NO_FULL_ID = -1;
    public static int DEFAULT_USERID = -1;
    public static long DEFAULT_CHANNELID = -1;

    public static int INSTALL_PACKAGES_REQUEST_CODE = 10086;

    /**
     * 下载路径
     */
    public static String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";


    public static ArrayList<String> G200_MODELS = new ArrayList();
    public static boolean IS_G200 = false;
    public static boolean IS_G100 = false;

    {
        G200_MODELS.add("MSM8996 for arm64");
        G200_MODELS.add("G200");

        IS_G200 = G200_MODELS.contains(Build.MODEL);
        IS_G100 = Build.BRAND == "HiAR";
    }

    public static class MirrorMode {
        public static int VIDEO_MIRROR_MODE_ENABLED = 0;
        public static int VIDEO_MIRROR_MODE_DISABLED = 1;
    }


    public static class VideoFMT {
        public static int VIDEO_FMT_TEXTURE_OES = 0;
        public static int VIDEO_FMT_NV21 = 1;
    }
}
