package com.example.mediaengine.exception;

/**
 * + * Created by hujun on 18/8/27.
 * +
 */

public class ErrorCode {
    public static final int ENGINE_INIT_ERROR = -1;
    public static final int ENGINE_JOIN_CHANNEL_ERROR = -2;
    public static final int ENGINE_CAMERA_ERROR = -3;
    public static final int ENGINE_LEAVE_ERROR = -4;
    public static final int ENGINE_SET_SECOND_RESOLUTION_ERROR = -5;
    public static final int NOT_IN_CHANNEL_ERROR = -6;
    public static final int CAN_NOT_FREEZE_ERROR = -7;
    public static final int DOWNLOAD_IMAGE_ERROR = -8;
    public static final int ENGINE_RESET_CONFIG_ERROR = -9;
    public static final int ENGINE_SWITCH_CAMERA_ERROR = -10;

    public static final int ENGINE_NO_SECOND_RESOLUTION_ERROR = -11;
    public static final int ENGINE_SET_CONFIG_ERROR = -12;
    public static final int ENGINE_CAMERA_OPEN_ERROR = -13;
    public static final int ENGINE_VIDEO_TOO_MUCH_ERROR = -14;
    public static final int ENGINE_NO_CONFIG_ERROR = -15;
    public static final int ENGINE_FLASH_ERROR = -16;

    public static final int INOTHER_STATE_ERROR = -100;
    public static final int NO_FULL_SCREEEN_ERROR = -101;
    public static final int CONFIG_SEND_MESSAGE_ERROR = -102;
    public static final int VIDEO_ROUTE_LIMITED_ERROR = -103;
    public static final int IS_DOWNLOADING = -104;
    public static final int UPLOAD_FAIL = -105;
    public static final int PULL_STREAM_ERROR = -106;
    public static final int PULL_STREAM_ERROR_NO_VIDEO = -107;
    public static final int PULL_STREAM_ERROR_NO_PRI = -108;

    public static final int ENGINE_SEND_TCP_MESSAGE_ERROR = -200;
    public static final int ENGINE_SEND_UDP_MESSAGE_ERROR = -201;
    public static final int UPLOAD_IMAGE_ERROR = -202;
    public static final int CAMERA_NOT_OPEN= -203;
    public static final int CAN_NOT_CLOSE_CAMERA_WHEN_MARK= -204;

    public static final int SHARE_FILE_UPLOAD_ERROR = -300;
    public static final int SHARE_FILE_SEND_MSG_ERROR = -301;

    public static final int BAD_CONNECTION = -400;
    public static final int BAD_CONNECTION_INVALID = -401;

    public static final int VOICE_MODE = -500;
}

