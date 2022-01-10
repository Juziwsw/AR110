package com.example.mediaengine.entity;

/**
 * Created by hujun on 18/8/30.
 */

public class FrameData {
    public static final int YUV420 = 1;
    public static final int YUV420SP = 2;

    private byte[] data;
    private int width;
    private int height;
    private int len;
    private long userId;
    private long time;
    private int format;
    private boolean mirror;

    //TODO : 旋转角度
    private int rotation;

    public FrameData(byte[] data, int len, int width, int height, long userId, long time) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.len = len;
        this.userId = userId;
        this.time = time;
        this.format = YUV420SP;
        this.mirror = false;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
