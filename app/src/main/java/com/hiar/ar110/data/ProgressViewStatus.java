package com.hiar.ar110.data;

/**
 * 带进度的View  liveData状态信息
 * author: lmh
 * date: 2021/4/14 17:34
 */
public class ProgressViewStatus {

    public static final int CANCEL = 0;//取消
    public static final int SHOW = 1;//显示
    public static final int UPDATE = 2;//更新

    private int state;//状态
    private int maxProgress;//最大进度
    private int progress;//进度
    private String filename;

    public ProgressViewStatus(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
