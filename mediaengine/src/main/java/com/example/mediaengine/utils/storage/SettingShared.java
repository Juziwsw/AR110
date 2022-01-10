package com.example.mediaengine.utils.storage;

import android.content.Context;
import androidx.annotation.NonNull;


/**
 * @author xujiangang
 */
public final class SettingShared {

    private SettingShared() {
    }

    public static final String TAG = "SettingShared";

    public static final String KEY_ENABLE_AUTOMATIC_ANSWER = "enableAutoAnswer";
    public static final String KEY_ENABLE_SPEAKER = "enableSpeaker";
    public static final String KEY_ENABLE_MIC = "enableMic";
    public static final String KEY_ENABLE_CAMERA = "enableCamera";
    public static final String KEY_CALL_RESOLUTION = "videoResolution";
    public static final String KEY_ENABLE_SMALL_WINDOW = "enableSmallWindow";
    public static final String KEY_ENABLE_HARDWARE_ENCODE = "enableHardwareEncode";
    public static final String KEY_ENABLE_HARDWARE_DECODE = "enableHardwareDecode";
    public static final String KEY_ENABLE_RETOUCH = "enableRetouch";
    public static final String KEY_ENABLE_DEBUG_MODE = "enableDebugMode";
    public static final String KEY_DEFAULT_PREVIEW = "preview";
    public static final String KEY_ENABLE_FULLSCREEN = "enableFullScreen";
    public static final String KEY_ENABLE_TCP_TRANSFER_MODE = "tcpTransferMode";
    public static final String KEY_CALL_FPS = "callFps";
    public static final String KEY_CALL_MAX_BPS = "callMaxBps";
    public static final String KEY_CALL_MIN_BPS = "callMinBps";
    public static final String KEY_INITIALIZE = "initialize";
    public static final String KEY_NEED_UPDATE = "needUpdate";
    public static final String KEY_SMART_DEVICE = "enableSmartDevice";
    public static final String KEY_USB_CAMERA = "enableUSBCamera";


    public static final String KEY_CLUB_ID = "sessionId";

    public static final int RESOLUTION_1080 = 2;
    public static final int RESOLUTION_720 = 1;
    public static final int RESOLUTION_360 = 0;

    public static boolean isFirst(@NonNull Context context) {
        boolean flag = SharedWrapper.with(context, TAG).getBoolean(KEY_INITIALIZE, true);
        SharedWrapper.with(context, TAG).setBoolean(KEY_INITIALIZE, false);
        return flag;
    }

    public static boolean isEnableSmartDevice(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_SMART_DEVICE, false);
    }

    public static void setEnableSmartDevice(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_SMART_DEVICE, enable);
    }

    public static boolean isEnableUSBCamera(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_USB_CAMERA, false);
    }

    public static void setEnableUSBCamera(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_USB_CAMERA, enable);
    }

    public static boolean isDefaultFullPreview(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_DEFAULT_PREVIEW, false);
    }

    public static void setDefaultFullPreview(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_DEFAULT_PREVIEW, enable);
    }

    public static boolean isEnableAutoAnswer(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_AUTOMATIC_ANSWER, false);
    }

    public static void setEnableAutoAnswer(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_AUTOMATIC_ANSWER, enable);
    }

    public static void setCallResolution(@NonNull Context context, int resolution) {
        SharedWrapper.with(context, TAG).setInt(KEY_CALL_RESOLUTION, resolution);
    }

    public static int getCallResolution(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getInt(KEY_CALL_RESOLUTION, RESOLUTION_720);
    }

    public static void setTCPTransferMode(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_TCP_TRANSFER_MODE, enable);
    }

    public static boolean getTCPTransferMode(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_TCP_TRANSFER_MODE, false);
    }

    public static boolean isEnableSpeaker(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_SPEAKER, true);
    }

    public static void setEnableSpeaker(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_SPEAKER, enable);
    }

    public static boolean isEnableMic(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_MIC, true);
    }

    public static void setEnableMic(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_MIC, enable);
    }

    public static boolean isEnableCamera(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_CAMERA, false);
    }

    public static void setEnableCamera(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_CAMERA, enable);
    }

    public static boolean isEnableSmallWindow(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_SMALL_WINDOW, true);
    }

    public static void setEnableSmallWindow(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_SMALL_WINDOW, enable);
    }

    public static boolean isEnableHardwareEncode(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_HARDWARE_ENCODE, true);
    }

    public static void setEnableHardwareEncode(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_HARDWARE_ENCODE, enable);
    }

    public static boolean isEnableHardwareDecode(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_HARDWARE_DECODE, true);
    }

    public static void setEnableHardwareDecode(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_HARDWARE_DECODE, enable);
    }

    public static boolean isEnableRetouch(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_RETOUCH, true);
    }

    public static void setEnableRetouch(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_RETOUCH, enable);
    }

    public static boolean isEnableDebugMode(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_DEBUG_MODE, false);
    }

    public static void setEnableDebugMode(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_DEBUG_MODE, enable);
    }

    public static void setCallFps(@NonNull Context context, int resolution) {
        SharedWrapper.with(context, TAG).setInt(KEY_CALL_FPS, resolution);
    }

    public static int getCallFps(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getInt(KEY_CALL_FPS, 15);
    }

    public static void setCallMaxBps(@NonNull Context context, int resolution) {
        SharedWrapper.with(context, TAG).setInt(KEY_CALL_MAX_BPS, resolution);
    }

    public static int getCallMaxBps(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getInt(KEY_CALL_MAX_BPS, 0);
    }

    public static void setCallMinBps(@NonNull Context context, int resolution) {
        SharedWrapper.with(context, TAG).setInt(KEY_CALL_MIN_BPS, resolution);
    }

    public static int getCallMinBps(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getInt(KEY_CALL_MIN_BPS, 0);
    }

    public static boolean isEnableFullScreen(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_ENABLE_FULLSCREEN, false);
    }

    public static void setEnableFullScreen(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_ENABLE_FULLSCREEN, enable);
    }

    public static boolean isNeedUpdate(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getBoolean(KEY_NEED_UPDATE, false);
    }

    public static void setNeedUpdate(@NonNull Context context, boolean enable) {
        SharedWrapper.with(context, TAG).setBoolean(KEY_NEED_UPDATE, enable);
    }

    public static void setClubKey(@NonNull Context context, String clubKey) {
        SharedWrapper.with(context, TAG).setString(KEY_CLUB_ID, clubKey);
    }

    public static String getClubKey(@NonNull Context context) {
        return SharedWrapper.with(context, TAG).getString(KEY_CLUB_ID, "");
    }
}
