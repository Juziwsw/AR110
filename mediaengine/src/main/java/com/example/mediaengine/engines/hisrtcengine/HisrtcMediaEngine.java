package com.example.mediaengine.engines.hisrtcengine;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.hiscene.cctalk.cloudrtc.CloudRtcHandler;
import com.hiscene.cctalk.cloudrtc.CloudRtcService;
import com.hiscene.cctalk.video.Constants;

import java.io.File;

import io.reactivex.Observable;

/**
 * @author: liwenfei.
 * data: 2019/4/19 11:55.
 */
public class HisrtcMediaEngine extends AbstractMediaEngine implements IMediaEngine {
    private static final String TAG = "HisrtcMediaEngine";
    private MediaConfig mediaConfig;
    private EngineConfigInfo mEngineConfig;
    private Context mContext;
    private HisrtcModel model;
    private String mUserId;
    private LongSparseArray<MediaQualityData> qualityMap;
    private CloudRtcService mRtcEngine;
    private ChannelStatusListener mChannelStatusListener;
    private boolean inChannel = false;
    private HisVideoSource hisVideoSource;

    public HisrtcMediaEngine(Context context) {
        super(context);
        this.mContext = context;
        model = new HisrtcModel();
        hisVideoSource = new HisVideoSource();
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
        XLog.i(TAG, "init");
        mEngineConfig = engine;
        try {
            StringBuffer logPath = new StringBuffer();
            logPath.append(LOG_PATH)
                    .append(File.separator)
                    .append("hisrtc.log");
            String[] address = new String[mEngineConfig.getServerAddressCount()];
            int index = 0;
            for (String addr : mEngineConfig.getServerAddressList()) {
                address[index] = addr;
                XLog.i(TAG, "addr: %s", addr);
                index++;
            }
            mRtcEngine = CloudRtcService.create(mContext, mEngineConfig.getAppSecret(), true, mRtcEventHandler, 2, address, address.length - 1);
            CloudRtcService.setUseMusicMode(true);// AR110 项目使用媒体模式
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.setExternalCapture(hisVideoSource);
            mRtcEngine.setLogFile(logPath.toString());
            HisrtcHelper.getInstance().setRtcEngine(mRtcEngine);
        } catch (Exception e) {
            XLog.e(TAG, "init error:%s", e.getLocalizedMessage());
            mRtcEngine = null;
        }
    }

    @Override
    public String getVersion() {
        return CloudRtcService.getVersion();
    }

    @Override
    public boolean isInChannel(long channelId) {
        return inChannel;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void enterChannel(String userId, String channelId) {
        XLog.i(TAG, "enterChannel userId: %d,channelId: %d", userId, channelId);
        mUserId = userId;
        mRtcEngine.enableVideo();
        XLog.i(TAG, "enableVideo");
        inChannel = false;
        qualityMap = new LongSparseArray<>();
        int res = mRtcEngine.enterChannel(mEngineConfig.getAppKey(), channelId, "", Integer.parseInt(userId));
        if (res < 0) {
            XLog.e(TAG, "enterChannel error : %s", res);
        }
        setVideoConfig();
        if (Constant.IS_G200) {
            XLog.i(TAG, "G200");
            mRtcEngine.setParameters("{\"che.audio.enable.aec\":1}");//设置音频软件回声消除
            mRtcEngine.setParameters("{\"che.audio.aec-parameters.delay\":100}");//设置音频软件回声消除
        }
        startInputVideoFrame();
    }

    @Override
    public void leaveChannel() {
        stopInputVideoFrame();
        mRtcEngine.leaveChannel();
        mRtcEngine.disableVideo();
        inChannel = false;
    }

    @Override
    public void resumeChannel() {
    }

    @Override
    public void pauseChannel() {
    }

    @Override
    public void destroy() {
        XLog.i(TAG, "destroy");
        CloudRtcService.destroy();
        mediaConfig = null;
        model = null;
        if (qualityMap != null) {
            qualityMap.clear();
            qualityMap = null;
        }
    }

    @Override
    public boolean muteMic(boolean onOff) {
        int res = mRtcEngine.muteLocalAudioStream(onOff);
        return res >= 0;
    }

    @Override
    public boolean muteSpeaker(boolean onOff) {
        int res = mRtcEngine.muteAllRemoteAudioStreams(onOff);
        return res >= 0;
    }

    @Override
    public boolean switchCamera() {
        int res = mRtcEngine.switchCamera();
        return res >= 0;
    }

    @Override
    public void startInputVideoFrame() {
        XLog.i(TAG, "startInputVideoFrame");
        mRtcEngine.muteLocalVideoStream(false);
        mRtcEngine.startPreview();
    }

    private void setVideoConfig() {
        int height = mediaConfig.getHeight();
        int resolution = Constants.VIDEO_PROFILE_360P;
        switch (height) {
            case 720:
                resolution = Constants.VIDEO_PROFILE_720P;
                break;
            case 480:
                resolution = Constants.VIDEO_PROFILE_480P;
                break;
            case 360:
                resolution = Constants.VIDEO_PROFILE_360P;
                break;
            case 1080:
                resolution = Constants.VIDEO_PROFILE_1080P;
                break;
            default:
                break;
        }
        XLog.i(TAG, "setVideoProfile: " + resolution);
        mRtcEngine.setVideoProfile(resolution, false);
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, "");
    }

    @Override
    public boolean inputVideoFrameGLES(SurfaceTexture surfaceTexture, int texture, float[] matrix, int width, int height, int fmt, int mirror) {
        return false;
    }

    @Override
    public boolean inputVideoFrame(byte[] data, int len, int width, int height, int fmt, int mirror, long timestamp) {
        if (hisVideoSource != null) {
            hisVideoSource.inputVideoFrame(data, width, height, timestamp);
        }
        return true;
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
        XLog.i(TAG, "stopInputVideoFrame");
        mRtcEngine.muteLocalVideoStream(true);
        mRtcEngine.stopPreview();
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

    private CloudRtcHandler mRtcEventHandler = new CloudRtcHandler() {

        @Override
        public void OnDebugCallback(int i, String s, int i1, int i2) {

        }

        @Override
        public void onAudioVolumeIndication(AudioVolume[] audioVolumes, int i, int i1) {
//            XLog.i(TAG,"onAudioVolumeIndication");
        }

        @Override
        public void onFirstLocalVideoFrame(int i, int i1, int i2) {
            XLog.i(TAG, "onFirstLocalVideoFrame");
        }

        @Override
        public void onFirstRemoteVideoFrame(int i, int i1, int i2, int i3) {
            XLog.i(TAG, "onFirstRemoteVideoFrame");
        }

        @Override
        public void onFirstRemoteVideoDecoded(int i, int i1, int i2, int i3) {
            XLog.i(TAG, "onFirstRemoteVideoDecoded");
        }

        @Override
        public void onJoinChannelSuccess(String s, int i, int i1) {
            XLog.i(TAG, "onJoinChannelSuccess");
            onJoinOk();
        }

        @Override
        public void onLeaveChannel(RtcStats rtcStats) {
            XLog.i(TAG, "onLeaveChannel");
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            XLog.i(TAG, "onUserJoined: " + uid);
            if (mChannelStatusListener != null) {
                mChannelStatusListener.onUserJoined(uid);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            XLog.i(TAG, "onUserOffline: " + uid);
            if (mChannelStatusListener != null) {
                mChannelStatusListener.onUserOffline(uid);
            }
        }

        @Override
        public void onUserMuteVideo(int i, boolean b) {

        }

        @Override
        public void onRtcStats(RtcStats rtcStats) {

        }

        @Override
        public void onLastMileQuality(int i) {

        }

        @Override
        public void onRejoinChannelSuccess(String s, int i, int i1) {
            XLog.i(TAG, "onRejoinChannelSuccess");
        }

        @Override
        public void onWarning(int i) {

        }

        @Override
        public void onError(int i) {
            XLog.i(TAG, "onError: %d", i);
        }

        @Override
        public void onApiCallExecuted(String s, int i) {
            XLog.i(TAG, "onApiCallExecuted: %s %d", s, i);
        }

        @Override
        public void onConnectionLost() {
            XLog.i(TAG, "onConnectionLost");
        }

        @Override
        public void onConnectionInterrupted() {
            XLog.i(TAG, "onConnectionInterrupted");
        }

        @Override
        public void onStreamMessage(int i, int i1, byte[] data) {
        }

        @Override
        public void onLocalVideoStats(LocalVideoStats localVideoStats) {

        }

        @Override
        public void onRemoteVideoStats(RemoteVideoStats remoteVideoStats) {

        }
    };

    private void updateQuality(long userId, MediaQualityData qualityData) {
        if (qualityMap != null) {
            qualityMap.put(userId, qualityData);
        }
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
