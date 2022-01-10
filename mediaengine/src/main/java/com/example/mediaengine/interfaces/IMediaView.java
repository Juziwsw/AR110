package com.example.mediaengine.interfaces;


import com.example.mediaengine.entity.VideoView;

/**
 * Created by hujun on 18/8/30.
 */

public interface IMediaView {
    /**
     * 初始化view
     */
    void init();

    VideoView addVideoView(long userId);

    void removeVideoView(long userId);

    VideoView enterFullView(long userId);

    void exitFullView();

    void pauseVideoView(long userId) throws Exception;

    void resumeVideoView(long userId) throws Exception;

    void removeAllView();

    void pauseAllView();

    void resumeAllView();
}
