package com.example.mediaengine.entity;

/**
 * Created by hujun on 18/8/30.
 */

public class MediaConfig {
    //传输视频宽高
    private int width;
    private int height;
    private int maxBitRate;
    private int minBitRate;

    //小流视频宽高
    private int secondWidth;
    private int secondHeight;
    private int secondMaxBitRate;
    private int secondMinBitRate;

    private boolean hwEncode;
    private boolean hwDecode;

    private int fps;
    private boolean enableSecondStream;
    private int reportInterval;
    private boolean enableBeauty;

    private boolean frontCamera;

    public MediaConfig() {
        width = 840;
        height = 480;
        maxBitRate = 0;
        minBitRate = 0;
        secondWidth = 256;
        secondHeight = 144;
        secondMaxBitRate = 0;
        secondMinBitRate = 0;
        hwDecode = true;
        hwEncode = true;
        fps = 15;
        enableSecondStream = false;
        reportInterval = 5000;
        enableBeauty = true;
        frontCamera = true;
    }

    public boolean isFrontCamera() {
        return frontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        this.frontCamera = frontCamera;
    }

    public boolean isEnableBeauty() {
        return enableBeauty;
    }

    public void setEnableBeauty(boolean enableBeauty) {
        this.enableBeauty = enableBeauty;
    }

    public int getSecondMaxBitRate() {
        return secondMaxBitRate;
    }

    public void setSecondMaxBitRate(int secondMaxBitRate) {
        this.secondMaxBitRate = secondMaxBitRate;
    }

    public int getSecondMinBitRate() {
        return secondMinBitRate;
    }

    public void setSecondMinBitRate(int secondMinBitRate) {
        this.secondMinBitRate = secondMinBitRate;
    }

    public int getReportInterval() {
        return reportInterval;
    }

    public void setReportInterval(int reportInterval) {
        this.reportInterval = reportInterval;
    }

    public boolean isEnableSecondStream() {
        return enableSecondStream;
    }

    public void setEnableSecondStream(boolean enableSecondStream) {
        this.enableSecondStream = enableSecondStream;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
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

    public int getSecondWidth() {
        return secondWidth;
    }

    public void setSecondWidth(int secondWidth) {
        this.secondWidth = secondWidth;
    }

    public int getSecondHeight() {
        return secondHeight;
    }

    public void setSecondHeight(int secondHeight) {
        this.secondHeight = secondHeight;
    }

    public int getMaxBitRate() {
        return maxBitRate;
    }

    public void setMaxBitRate(int maxBitRate) {
        this.maxBitRate = maxBitRate;
    }

    public int getMinBitRate() {
        return minBitRate;
    }

    public void setMinBitRate(int minBitRate) {
        this.minBitRate = minBitRate;
    }

    public boolean isHwEncode() {
        return hwEncode;
    }

    public void setHwEncode(boolean hwEncode) {
        this.hwEncode = hwEncode;
    }

    public boolean isHwDecode() {
        return hwDecode;
    }

    public void setHwDecode(boolean hwDecode) {
        this.hwDecode = hwDecode;
    }
}
