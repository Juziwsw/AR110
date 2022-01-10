package com.example.mediaengine.entity;

/**
 * @author hujun
 * @date 18/7/18
 */

public final class MediaQualityData {
    public static final int VERY_GOOD = 0;
    public static final int GOOD = 1;
    public static final int NORMAL = 2;
    public static final int BAD = 3;
    public static final int VERY_BAD = 4;
    public static final int WORST = 5;

    public long userId;
    public long channelId;
    public int bitRate;
    public int lossRate;
    public int block;
    public int fps;
    public int width;
    public int height;
    public int delay;

    public MediaQualityData(long userId) {
        this.userId = userId;
    }

    public MediaQualityData(long userId, long channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }


    private MediaQualityData() {
    }


}
