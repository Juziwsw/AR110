package com.example.mediaengine;

import com.example.mediaengine.entity.VideoView;
import com.example.mediaengine.interfaces.IMediaView;

public class MediaViewHolder {
    private IMediaView mediaView;
    private long fullScreenId = -1;

    private static class SingletonHolder {
        static final MediaViewHolder _instance = new MediaViewHolder();
    }

    public static MediaViewHolder Instance() {
        return SingletonHolder._instance;
    }

    public IMediaView getMediaView() {
        return mediaView;
    }

    public void setMediaView(IMediaView mediaView) {
        this.mediaView = mediaView;
    }

    public boolean isInFullScreen() {
        return fullScreenId > -1;
    }

    public long getFullScreenId() {
        return fullScreenId;
    }

    public VideoView enterFullScreen(long userId) {
        fullScreenId = userId;
        mediaView.pauseAllView();
        return mediaView.enterFullView(fullScreenId);
    }

    public void exitFullScreen() {
        mediaView.exitFullView();
        fullScreenId = -1;
        mediaView.resumeAllView();
    }
}
