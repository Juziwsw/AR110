package com.hiar.mybaselib.utils;

import com.hileia.common.utils.XLog;

/**
 * author: liwf
 * date: 2021/4/25 14:23
 */
public class AR110Log {
    public static void i(String tag, String msg) {
        XLog.i(tag, "%s", msg);
    }

    public static void e(String tag, String msg) {
        XLog.e(tag, "%s", msg);
    }

    public static void w(String tag, String msg) {
        XLog.w(tag, "%s", msg);
    }

    public static void d(String tag, String msg) {
        XLog.d(tag, "%s", msg);
    }

    public static void i(String tag, String fmt, Object... args) {
        XLog.i(tag, fmt, args);
    }

    public static void e(String tag, String fmt, Object... args) {
        XLog.e(tag, fmt, args);
    }

    public static void w(String tag, String fmt, Object... args) {
        XLog.w(tag, fmt, args);
    }

    public static void d(String tag, String fmt, Object... args) {
        XLog.d(tag, fmt, args);
    }
}
