package com.hiscene.imui.util;

import java.text.DecimalFormat;

/**
 * @author hujun
 * @date 16/10/12
 */
public final class FileUtils {
    // 日志 TAG
    private static final String TAG = FileUtils.class.getSimpleName();
    // 换行字符串
    private static final String NEW_LINE_STR = System.getProperty("line.separator");

    /**
     * 传入对应的文件大小, 返回转换后文件大小
     *
     * @param fileSize 文件大小
     * @return 文件大小转换字符串
     */
    public static String formatFileSize(final double fileSize) {
        // 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeStr;
        if (fileSize <= 0) {
            fileSizeStr = "0B";
        } else if (fileSize < 1024) {
            fileSizeStr = df.format(fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeStr = df.format(fileSize / 1024) + "KB";
        } else if (fileSize < 1073741824) {
            fileSizeStr = df.format(fileSize / 1048576) + "MB";
        } else if (fileSize < 1099511627776d) {
            fileSizeStr = df.format(fileSize / 1073741824) + "GB";
        } else {
            fileSizeStr = df.format(fileSize / 1099511627776d) + "TB";
        }
        return fileSizeStr;
    }
}
