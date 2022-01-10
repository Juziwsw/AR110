package com.hiar.ar110.data;


import android.os.Build;

import java.util.ArrayList;

/**
 * @author xujiangang
 */
public class Constants {
    public static String TAG_HILEIA_CUSTOM_VIEW = "hileia_custom_view";
    public static String THEME_LIGHT = "themeLight";
    public static String THEME_DARK = "themeDark";
    public static int MAX_CALL_MEMBER = 6;
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    private static ArrayList G200_MODELS = new ArrayList();
    public static boolean IS_G200 = false;
    public static boolean IS_G100 = false;

    static {
        G200_MODELS.add("MSM8996 for arm64");
        G200_MODELS.add("G200");

        IS_G200 = G200_MODELS.contains(Build.MODEL);
        IS_G100 = Build.BRAND == "HiAR";
    }
}
