package com.example.mediaengine.interfaces;

import android.graphics.SurfaceTexture;

import com.example.mediaengine.entity.EngineConfigInfo;
import com.example.mediaengine.entity.FrameData;
import com.example.mediaengine.entity.MediaConfig;
import com.example.mediaengine.entity.MediaQualityData;
import com.example.mediaengine.entity.SurfaceContext;

import io.reactivex.Observable;

/**
 * @author hujun
 * @date 18/8/30
 */

public interface IMediaEngine {
    interface ChannelStatusListener {
        void onUserJoined(long uid);

        void onUserOffline(long uid);
    }

    /**
     * @param listener 房间内状态监听
     */
    void setChannelStatusListener(ChannelStatusListener listener);

    /**
     * @return
     */
    void setMediaConfig(MediaConfig media);

    /**
     * 初始化
     *
     * @return
     */
    void init(EngineConfigInfo engineConfigInfo);

    /**
     * 获取引擎版本
     *
     * @return
     */    String getVersion();

    /**
     * 是否在房间内
     *
     * @return
     */
    boolean isInChannel(long channelId);

    /**
     * 进入音视频Channel
     *
     * @param userId    用户id
     * @param channelId 房间名
     * @return
     */
    void enterChannel(String userId, String channelId);

    /**
     * 离开音视频Channel
     *
     * @return
     */
    void leaveChannel();

    /**
     * 恢复音视频Channel
     *
     * @return
     */
    void resumeChannel();

    /**
     * 暂停音视频Channel
     *
     * @return
     */
    void pauseChannel();

    /**
     * 销毁引擎
     *
     * @return
     */
    void destroy();

    /**
     * 切换麦克风开关
     *
     * @return
     */
    boolean muteMic(boolean onOff);

    /**
     * 切换speaker开关
     *
     * @return
     */
    boolean muteSpeaker(boolean onOff);

    /**
     * 切换前后摄像头
     *
     * @return
     */
    boolean switchCamera();

    /**
     * Texture 方式发送视频数据
     *
     * @param texture
     * @param matrix
     * @param fmt
     * @return bool
     */
    boolean inputVideoFrameGLES(SurfaceTexture surfaceTexture, int texture, float[] matrix, int width, int height, int fmt, int mirror);

    /**
     * byte[] 方式发送视频数据
     *
     * @param data
     * @param len
     * @param fmt
     * @param timestamp
     * @return
     */
    boolean inputVideoFrame(byte[] data, int len, int width, int height, int fmt, int mirror, long timestamp);

    /**
     * @return
     */
    Observable<FrameData> listenFrameData();

    /**
     * @return
     */
    Observable<MediaQualityData> listenQualityData();

    /**
     * 停止发送视频数据
     *
     * @return
     */
    void stopInputVideoFrame();

    /**
     * 开始发送视频数据
     *
     * @return
     */
    void startInputVideoFrame();

    /**
     * 暂停接受视频
     *
     * @param userId
     * @return
     */
    void pauseReceiveVideo(long userId);

    /**
     * 重新开始接受视频
     *
     * @param userId
     * @return
     */
    void resumeReceiveVideo(long userId);

    /**
     * 屏蔽远端视频流
     *
     * @param flag
     * @return
     */
    int muteAllRemoteVideoStreams(boolean flag);

    /**
     * 切换视频分辨率
     *
     * @param userId
     * @param fullResolution
     * @return
     */
    void switchVideoResolution(long userId, boolean fullResolution);

    /**
     * Surface上下文
     *
     * @return
     */
    SurfaceContext getSurfaceContext();

    /**
     * 由于不同引擎不同，有的引擎是可以自己截图的
     *
     * @param userId
     * @return true 引擎自己有截图功能；false 引擎没有截图功能，需要自己做
     */
    boolean freeze(long userId);
}
