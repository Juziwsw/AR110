package com.example.mediaengine.engines.agoraengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.LongSparseArray;

import com.example.mediaengine.Constant;
import com.example.mediaengine.entity.EngineConfigInfo;
import com.example.mediaengine.entity.FrameData;
import com.example.mediaengine.entity.MediaConfig;
import com.example.mediaengine.entity.MediaQualityData;
import com.example.mediaengine.entity.SurfaceContext;
import com.example.mediaengine.interfaces.AbstractMediaEngine;
import com.example.mediaengine.interfaces.IMediaEngine;
import com.hileia.common.utils.XLog;

import java.io.File;
import io.reactivex.Observable;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoEncoderConfiguration;

/**
 * Created by hujun on 2018/11/6.
 */
public class AgoraMediaEngine extends AbstractMediaEngine implements IMediaEngine {
    private static final String TAG = "AgoraMediaEngineTAG";
    private MediaConfig mediaConfig;
    private EngineConfigInfo mEngineConfig;
    private Context mContext;
    private AgoraModel model;
    private LongSparseArray<MediaQualityData> qualityMap;
    private RtcEngine mRtcEngine;
    private ChannelStatusListener mChannelStatusListener;
    private boolean inChannel = false;
    private VideoSource videoSource;

    public AgoraMediaEngine(Context context) {
        super(context);
        this.mContext = context;
        model = new AgoraModel();
        videoSource = new VideoSource();
    }

    @Override
    public void setMediaConfig(MediaConfig media) {
        XLog.d(TAG, "setMediaConfig");
        mediaConfig = media;
    }

    @Override
    public void setChannelStatusListener(ChannelStatusListener listener) {
        mChannelStatusListener = listener;
    }

    @Override
    public void init(EngineConfigInfo engine) {
        mEngineConfig = engine;
        try {
            StringBuffer logPath = new StringBuffer();
            logPath.append(LOG_PATH)
                    .append(File.separator)
                    .append("agora.log");
            //目前 Agora Native SDK 只支持一个 RtcEngine 实例，每个 App 仅创建一个 RtcEngine 对象
            mRtcEngine = RtcEngine.create(mContext, mEngineConfig.getAppKey(), mRtcEventHandler);
            mRtcEngine.setLogFile(logPath.toString());
            mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD,Constants.AUDIO_SCENARIO_GAME_STREAMING);
            AgoraHelper.getInstance().setRtcEngine(mRtcEngine);
            AgoraHelper.getInstance().setFrameListener(mFrameListener);
        } catch (Exception e) {
            XLog.e(TAG, "init error:"+ e.getLocalizedMessage());
            mRtcEngine = null;
        }
    }

    @Override
    public String getVersion() {
        return "AGORA_" + RtcEngine.getSdkVersion();
    }

    @Override
    public boolean isInChannel(long channelId) {
        return inChannel;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void enterChannel(String userId, String channelId) {
        XLog.i(TAG, "enterChannel: "+ channelId);
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);//设置频道模式为通信模式,该方法必须在 加入频道 前调用
        inChannel = false;
        qualityMap = new LongSparseArray<>();
        mRtcEngine.setVideoSource(videoSource);
        setVideoConfig();//配置引擎视频方向/宽高/帧率/比特率
        mRtcEngine.enableVideo();//启用视频模块,成功调用该方法后，远端会触发 onUserEnableVideo(true) 回调
//        mRtcEngine.enableWebSdkInteroperability(true);//打开与 Web SDK 的互通（仅在直播下适用）
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        if (Constant.IS_G200) {
            mRtcEngine.setParameters("{\"che.audio.audioMode\":0}");
            mRtcEngine.setParameters("{\"che.audio.recordingDevice\":1}");
            mRtcEngine.setParameters("{\"che.audio.audioSampleRate\":48000}");
            mRtcEngine.setParameters("{\"che.audio.stream_type\":3}");
            mRtcEngine.adjustPlaybackSignalVolume(200);//眼镜端播放声音放大1.5倍
            mRtcEngine.setParameters("{\"che.audio.opensl\":false}");
        }
        int res = mRtcEngine.joinChannel(null, channelId, "", Integer.parseInt(userId));
        if (res < 0) {
            XLog.e(TAG, "enterChannel error : "+ res);
        }
    }

    @Override
    public void leaveChannel() {
        XLog.i(TAG, "leaveChannel");
        mRtcEngine.leaveChannel();
        mRtcEngine.disableVideo();
        inChannel = false;
    }

    /**
     * 该方法暂停播放伴奏。请在频道内调用该方法。
     */
    @Override
    public void resumeChannel() {
        mRtcEngine.resumeAudioMixing();
    }


    /**
     * 该方法恢复混音，继续播放伴奏。请在频道内调用该方法。
     */
    @Override
    public void pauseChannel() {
        mRtcEngine.pauseAudioMixing();
    }

    @Override
    public void destroy() {
        RtcEngine.destroy();
        mediaConfig = null;
        model = null;
        videoSource = null;
        if (qualityMap != null) {
            qualityMap.clear();
            qualityMap = null;
        }
    }

    @Override
    public boolean muteMic(boolean onOff) {
        int res = mRtcEngine.muteLocalAudioStream(onOff);//是否发布本地音频流
        return res >= 0;
    }

    @Override
    public boolean muteSpeaker(boolean onOff) {
        int res = mRtcEngine.muteAllRemoteAudioStreams(onOff);//是否接收并播放所有远端音频流
        return res >= 0;
    }

    @Override
    public boolean switchCamera() {
        int res = mRtcEngine.switchCamera();
        return res >= 0;
    }

    @Override
    public void startInputVideoFrame() {
        XLog.d(TAG, "startInputVideoFrame");
        mRtcEngine.muteLocalVideoStream(false);//是否发布本地视频流
    }

    private void setVideoConfig() {
        VideoEncoderConfiguration.ORIENTATION_MODE
                orientationMode =
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE;//竖屏

        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(mediaConfig.getWidth(), mediaConfig.getHeight());//宽高

        VideoEncoderConfiguration.FRAME_RATE frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;//缺省帧率
        switch (mediaConfig.getFps()) {
            case 1:
            case 2:
            case 3:
            case 4:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7;
                break;
            case 9:
            case 10:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10;
                break;
            case 15:
            case 16:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
                break;
            case 24:
            case 25:
            case 20:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24;
                break;
            case 30:
                frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30;
                break;
            default:
                break;
        }

        int bitrate = 0;
        if (mediaConfig.getMaxBitRate() != 0) {
            bitrate = mediaConfig.getMaxBitRate();
        }

        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration(dimensions, frameRate, bitrate, orientationMode);
        XLog.d(TAG, "startInputVideoFrame w =%s,h=%s,frame=%s,bitrate=%s,orientation="+
                dimensions.width, dimensions.height, frameRate, bitrate, orientationMode);
        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
    }

    @Override
    public boolean inputVideoFrameGLES(SurfaceTexture surfaceTexture, int texture, float[] matrix, int width, int height, int fmt, int mirror) {
        return false;
    }

    @Override
    public boolean inputVideoFrame(byte[] data, int len, int width, int height, int fmt, int mirror, long timestamp) {
        if (videoSource != null) {
            videoSource.inputVideoFrame(data, len, width, height, fmt, mirror, timestamp);
        }
        return false;
    }


    @Override
    public Observable<FrameData> listenFrameData() {
        return mFrameListener;
    }

    @Override
    public Observable<MediaQualityData> listenQualityData() {
        return mQualityListener;
    }

    @Override
    public void stopInputVideoFrame() {
        mRtcEngine.muteLocalVideoStream(true);
    }

    @Override
    public void pauseReceiveVideo(long userId) {
    }

    @Override
    public void resumeReceiveVideo(long userId) {
    }

    @Override
    public int muteAllRemoteVideoStreams(boolean flag) {
        return mRtcEngine.muteAllRemoteVideoStreams(flag);
    }

    @Override
    public void switchVideoResolution(long userId, boolean fullResolution) {
    }

    @Override
    public SurfaceContext getSurfaceContext() {
        return new SurfaceContext(new SurfaceTexture(-1), -1);
    }

    @Override
    public boolean freeze(long userId) {
        return false;
    }

    private void onJoinOk() {
        mRtcEngine.createDataStream(false, false);
        inChannel = true;
    }

    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            XLog.e(TAG, "onWarning : " + warn);
        }

        @Override
        public void onError(int err) {
            XLog.e(TAG, "onError : "+ err);

        }

        @Override
        public void onApiCallExecuted(int error, String api, String result) {
            super.onApiCallExecuted(error, api, result);
//            XLog.d(TAG, "onApiCallExecuted error=%s,api=%s,result="+ error, api, result);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            XLog.i(TAG, "onJoinChannelSuccess channel="+channel+ "uid="+ uid+"elapsed="+  elapsed);
            onJoinOk();
        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
            XLog.i(TAG, "onRejoinChannelSuccess channel="+ channel+"uid="+uid+ "elapsed="+ elapsed);
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            XLog.i(TAG, "onLeaveChannel");
        }

        @Override
        public void onClientRoleChanged(int oldRole, int newRole) {
            super.onClientRoleChanged(oldRole, newRole);
            XLog.i(TAG, "onClientRoleChanged o="+oldRole+ "n="+  newRole);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            if (mChannelStatusListener != null) {
                mChannelStatusListener.onUserJoined(uid);
            }
            XLog.i(TAG, "onUserJoined id="+uid+ "elapsed="+  elapsed);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            if (mChannelStatusListener != null) {
                mChannelStatusListener.onUserOffline(uid);
            }
            XLog.i(TAG, "onUserOffline id="+ "reason="+ uid, reason);
        }

        @Override
        public void onLastmileQuality(int quality) {
            super.onLastmileQuality(quality);
            XLog.i(TAG, "onLastmileQuality : "+ quality);
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            XLog.i(TAG, "onConnectionLost");
        }

        @Override
        public void onTokenPrivilegeWillExpire(String token) {
            super.onTokenPrivilegeWillExpire(token);
            XLog.d(TAG, "onTokenPrivilegeWillExpire "+ token);
        }

        @Override
        public void onRequestToken() {
            super.onRequestToken();
            XLog.d(TAG, "onRequestToken");
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
        }

        @Override
        public void onActiveSpeaker(int uid) {
            super.onActiveSpeaker(uid);
            XLog.d(TAG, "onActiveSpeaker id="+ uid);
        }

        @Override
        public void onFirstLocalAudioFrame(int elapsed) {
            super.onFirstLocalAudioFrame(elapsed);
            XLog.d(TAG, "onFirstLocalAudioFrame : "+ elapsed);
        }

        @Override
        public void onFirstRemoteAudioFrame(int uid, int elapsed) {
            super.onFirstRemoteAudioFrame(uid, elapsed);
            XLog.d(TAG, "onFirstRemoteAudioFrame uid="+uid+ "elapsed="+  elapsed);
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
            super.onFirstLocalVideoFrame(width, height, elapsed);
            XLog.d(TAG, "onFirstLocalVideoFrame w=%"+width+ "h="+height+ "el="+ elapsed);
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            XLog.d(TAG, "onFirstRemoteVideoFrame id="+ uid+" w="+width+ "h="+ height+"el="+ elapsed);
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
            XLog.d(TAG, "onUserMuteAudio id="+ uid+ "muted="+ muted);
        }

        @Override
        public void onLocalVideoStateChanged(int localVideoState, int error) {
            super.onLocalVideoStateChanged(localVideoState, error);
            XLog.d(TAG, "onLocalVideoStateChanged localVideoState="+ localVideoState+ " error="+  error);
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            XLog.d(TAG, "onRemoteVideoStateChanged id="+ " state=%s, reason="+ uid, state, reason);
        }

        @Override
        public void onLocalPublishFallbackToAudioOnly(boolean isFallbackOrRecover) {
            super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover);
            XLog.d(TAG, "onLocalPublishFallbackToAudioOnly "+ isFallbackOrRecover);
        }

        @Override
        public void onRemoteSubscribeFallbackToAudioOnly(int uid, boolean isFallbackOrRecover) {
            super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover);
            XLog.d(TAG, "onRemoteSubscribeFallbackToAudioOnly %s,"+ uid, isFallbackOrRecover);
        }

        @Override
        public void onAudioRouteChanged(int routing) {
            super.onAudioRouteChanged(routing);
            XLog.d(TAG, "onAudioRouteChanged "+ routing);
        }

        @Override
        public void onCameraFocusAreaChanged(Rect rect) {
            super.onCameraFocusAreaChanged(rect);
            XLog.d(TAG, "onCameraFocusAreaChanged");
        }

        @Override
        public void onRtcStats(RtcStats stats) {
            super.onRtcStats(stats);
//            XLog.d(TAG, "onRtcStats duration=%s,users="+ stats.totalDuration, stats.users);
        }

        /**
         * 通话中每个用户的网络上下行 last mile 质量报告回调。
         *
         * 该回调描述每个用户在通话中的 last mile 网络状态，其中 last mile 是指设备到 Agora 边缘服务器的网络状态。该回调每 2 秒触发一次。如果远端有多个用户/主播，该回调每 2 秒会被触发多次。
         *
         * @param uid 用户 ID。表示该回调报告的是持有该 ID 的用户的网络质量。当 uid 为 0 时，返回的是本地用户的网络质量。
         *
         * @param txQuality 该用户的上行网络质量，基于上行视频的发送码率、上行丢包率、平均往返时延和网络抖动计算。该值代表当前的上行网络质量，
         *                  帮助判断是否可以支持当前设置的视频编码属性。假设直播模式下上行码率是 1000 Kbps，那么支持 640 × 480 的分辨率、30 fps 的帧率没有问题，但是支持 1280 x 720 的分辨率就会有困难
         *
         * @param rxQuality 该用户的下行网络质量，基于下行网络的丢包率、平均往返延时和网络抖动计算
         */
        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            super.onNetworkQuality(uid, txQuality, rxQuality);
            XLog.d(TAG, "onNetworkQuality id=%s,txq=%s,rxq="+ uid, txQuality, rxQuality);
            if (uid == 0) {
                uid = model.transferUserId(0);
                rxQuality = txQuality;
            }
            MediaQualityData qualityData = getQuality(model.transferUserId(uid));
            switch (rxQuality) {
                case Quality.UNKNOWN://质量未知
                    qualityData.block = MediaQualityData.NORMAL;
                    break;
                case Quality.EXCELLENT://质量极好
                    qualityData.block = MediaQualityData.VERY_GOOD;
                    break;
                case Quality.GOOD://用户主观感觉和极好差不多，但码率可能略低于极好
                    qualityData.block = MediaQualityData.GOOD;
                    break;
                case Quality.POOR://用户主观感受有瑕疵但不影响沟通
                    qualityData.block = MediaQualityData.NORMAL;
                    break;
                case Quality.BAD://勉强能沟通但不顺畅
                    qualityData.block = MediaQualityData.BAD;
                    break;
                case Quality.VBAD://网络质量非常差，基本不能沟通
                    qualityData.block = MediaQualityData.VERY_BAD;
                    XLog.e(TAG, "onNetworkQuality VBAD");
                    break;
                case Quality.DOWN://网络连接断开，完全无法沟通
                    qualityData.block = MediaQualityData.WORST;
                    break;
                default:
                    break;
            }
            updateQuality(model.transferUserId(uid), qualityData);
        }

        /**
         * 通话中本地视频流的统计信息回调。
         *
         * 该回调描述本地设备发送视频流的统计信息，每 2 秒触发一次。
         * @param stats 本地视频统计数据
         */
        @Override
        public void onLocalVideoStats(LocalVideoStats stats) {
            super.onLocalVideoStats(stats);
            MediaQualityData qualityData = getQuality(0);
            qualityData.bitRate = stats.sentBitrate;
            qualityData.fps = stats.rendererOutputFrameRate;
            qualityData.width = stats.encodedFrameWidth;
            qualityData.height = stats.encodedFrameHeight;
            updateQuality(0, qualityData);
        }

        /**
         * 通话中远端视频流的统计信息回调。
         *
         * 该回调描述远端用户在通话中端到端的视频流状态，针对每个远端用户/主播每 2 秒触发一次。如果远端同时存在多个用户/主播，该回调每 2 秒会被触发多次。
         * @param stats 远端视频统计数据
         */
        @Override
        public void onRemoteVideoStats(RemoteVideoStats stats) {
            super.onRemoteVideoStats(stats);
            MediaQualityData qualityData = getQuality(model.transferUserId(stats.uid));
            qualityData.width = stats.width;
            qualityData.height = stats.height;
            qualityData.fps = stats.rendererOutputFrameRate;
            qualityData.bitRate = stats.receivedBitrate;
            qualityData.lossRate = stats.packetLossRate;
            updateQuality(model.transferUserId(stats.uid), qualityData);
        }

        @Override
        public void onRemoteAudioStats(RemoteAudioStats stats) {
            super.onRemoteAudioStats(stats);
        }

        @Override
        public void onAudioEffectFinished(int soundId) {
            super.onAudioEffectFinished(soundId);
            XLog.d(TAG, "onAudioEffectFinished " + soundId);
        }

        @Override
        public void onStreamPublished(String url, int error) {
            super.onStreamPublished(url, error);
            XLog.d(TAG, "onStreamPublished url=%s,error="+ url, error);
        }

        @Override
        public void onStreamUnpublished(String url) {
            super.onStreamUnpublished(url);
            XLog.d(TAG, "onStreamUnpublished : "+ url);
        }

        @Override
        public void onTranscodingUpdated() {
            super.onTranscodingUpdated();
            XLog.d(TAG, "onTranscodingUpdated");
        }

        @Override
        public void onStreamInjectedStatus(String url, int uid, int status) {
            super.onStreamInjectedStatus(url, uid, status);
            XLog.d(TAG, "onStreamInjectedStatus url=%s,uid=%s,status=%s ", url, uid, status);
        }

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
            super.onStreamMessage(uid, streamId, data);
        }

        @Override
        public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
            super.onStreamMessageError(uid, streamId, error, missed, cached);
            XLog.e(TAG, "onStreamMessageError uid=%s, streamId=%s, error=%s, missed=%s, cached="+
                    uid, streamId, error, missed, cached);
        }

        @Override
        public void onMediaEngineLoadSuccess() {
            super.onMediaEngineLoadSuccess();
            XLog.d(TAG, "onMediaEngineLoadSuccess");
        }

        @Override
        public void onMediaEngineStartCallSuccess() {
            super.onMediaEngineStartCallSuccess();
            XLog.d(TAG, "onMediaEngineStartCallSuccess");
        }

        @Override
        public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
            super.onVideoSizeChanged(uid, width, height, rotation);
            XLog.d(TAG, "onVideoSizeChanged uid=%s, width=%s, height=%s, rotation="+ uid, width, height, rotation);
        }
    };

    private void updateQuality(long userId, MediaQualityData qualityData) {
        if (qualityMap != null) {
            qualityMap.put(userId, qualityData);
        }
        mQualityListener.accept(qualityData);
    }

    private MediaQualityData getQuality(long userId) {
        if (qualityMap == null) {
            qualityMap = new LongSparseArray<>();
        }
        MediaQualityData qualityData = qualityMap.get(userId);
        if (qualityData == null) {
            qualityData = new MediaQualityData(userId);
        }
        return qualityData;
    }

}
