package com.example.mediaengine.engines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

import com.example.mediaengine.Constant;
import com.example.mediaengine.MediaViewHolder;
import com.example.mediaengine.engines.agoraengine.AgoraMediaEngine;
import com.example.mediaengine.engines.agoraengine.AgoraViewManager;
import com.example.mediaengine.engines.hisrtcengine.HisrtcMediaEngine;
import com.example.mediaengine.engines.hisrtcengine.HisrtcViewManager;
import com.example.mediaengine.entity.EngineConfigInfo;
import com.example.mediaengine.entity.MediaQualityData;
import com.example.mediaengine.interfaces.ICameraEngine;
import com.example.mediaengine.interfaces.IMediaEngine;
import com.example.mediaengine.interfaces.IMediaView;
import com.example.mediaengine.utils.ImageUtils;
import com.example.mediaengine.utils.Luban;
import com.example.mediaengine.utils.SnapshotUtil;
import com.hiscene.cctalk.video.AudioFrameObserver;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;


public class MediaEngineHolder {
    private IMediaEngine mediaEngine;
    private ICameraEngine cameraEngine;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SnapshotUtil snapshotUtil;
    private int cameraErrorCode = 0;
    private Context mContext;

    private static class SingletonHolder {
        static final MediaEngineHolder _instance = new MediaEngineHolder();
    }

    public static MediaEngineHolder Instance() {
        return SingletonHolder._instance;
    }

    public int getCameraErrorCode() {
        return cameraErrorCode;
    }

    public void setCameraErrorCode(int errorCode) {
        this.cameraErrorCode = errorCode;
    }

    public void setMediaEngine(IMediaEngine mediaEngine) {
        this.mediaEngine = mediaEngine;
    }

    public IMediaEngine getMediaEngine() {
        return mediaEngine;
    }

    public ICameraEngine getCameraEngine() {
        return cameraEngine;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private ICameraEngine.OnNewFrameListener mFrameListener = new ICameraEngine.OnNewFrameListener() {
        @Override
        public void onNewFrame(byte[] data, int width, int height, int rotation) {
            int mirror;
            if (cameraEngine.isFrontCamera()) {
                mirror = Constant.MirrorMode.VIDEO_MIRROR_MODE_ENABLED;
            } else {
                mirror = Constant.MirrorMode.VIDEO_MIRROR_MODE_DISABLED;
            }
            mediaEngine.inputVideoFrame(data, data.length, width, height, Constant.VideoFMT.VIDEO_FMT_NV21, mirror, System.currentTimeMillis());
        }

        @Override
        public void onTextureFrame(SurfaceTexture surfaceTexture, float[] matrix, int width, int height, int rotation) {

        }

        @Override
        public void onError(int i) {
            cameraErrorCode = i;
        }
    };

    public void enterChannel(String userId, String channelId, String photoPath, IMediaEngine.ChannelStatusListener listener) {
        if (listener != null) {
            mediaEngine.setChannelStatusListener(listener);
        }
        mediaEngine.enterChannel(userId, channelId);
        compositeDisposable.add(mediaEngine.listenFrameData().subscribe(frameData -> {
            if (snapshotCallback != null) {
                if (frameData.getUserId() == snapshotId) {
                    Bitmap bitmap = snapshotUtil.freezeData(frameData);
                    File file = new File(photoPath);
                    ImageUtils.saveImageToExternal(mContext, bitmap, true, file);
                    Luban.with(mContext).get(file.getAbsolutePath());
                    snapshotCallback.snapshot(photoPath);
                    snapshotCallback = null;
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
        }));
        compositeDisposable.add(mediaEngine.listenQualityData().subscribe(mediaQuality -> {
            if (qualityCallback != null) {
                qualityCallback.Quality(mediaQuality);
                qualityCallback = null;
            }
        }));
    }

    public void leaveChannel(boolean useExternal) {
        HiPlugin.getInstance().clearCamera();
        mediaEngine.leaveChannel();
        compositeDisposable.clear();
    }

    public void switchCamera() {
        cameraErrorCode = 0;
        cameraEngine = HiPlugin.getInstance().nextCameraEngine(true);
        cameraEngine.setOnNewFrameListener(mFrameListener);
        cameraEngine.openCamera(false, 0, mediaEngine.getSurfaceContext().getSurfaceTexture());
        mediaEngine.startInputVideoFrame();
    }

    public void closeCamera() {
        mediaEngine.stopInputVideoFrame();
        if (cameraEngine != null) {
            cameraEngine.closeCamera();
            cameraEngine.setOnNewFrameListener(null);
            cameraEngine = null;
        }
        cameraErrorCode = 0;
    }

    public void reverseCamera() {
        if (cameraEngine != null) {
            cameraEngine.closeCamera();
        }
        cameraEngine = HiPlugin.getInstance().nextCameraEngine(false);
        cameraEngine.setOnNewFrameListener(mFrameListener);
        cameraEngine.openCamera(false, 0, mediaEngine.getSurfaceContext().getSurfaceTexture());
    }

    public void switchCamera(ICameraEngine engine) {
        if (cameraEngine != null) {
            cameraEngine.closeCamera();
        }
        cameraEngine = engine;
        cameraEngine.setOnNewFrameListener(mFrameListener);
        cameraEngine.openCamera(false, 0, mediaEngine.getSurfaceContext().getSurfaceTexture());
    }

    public void switchFlashlight() {
        if (cameraEngine != null) {
            cameraEngine.switchFlashLight(mContext);
        }
    }

    public void closeFlashLight() {
        if (cameraEngine != null) {
            cameraEngine.closeFlashLight(mContext);
        }
    }

    public void handleZoom(float ratio) {
        if (cameraEngine != null) {
            cameraEngine.handleZoom(ratio);
        }
    }

    public void handleFocusMetering(int centerX, int centerY, ICameraEngine.FocusCallback callback) {
        if (cameraEngine != null) {
            cameraEngine.handleFocusMetering(centerX, centerY, callback);
        }
    }

    public interface QualityCallback {
        void Quality(MediaQualityData mediaQuality);
    }

    public interface SnapshotCallback {
        void snapshot(String path);
    }

    private QualityCallback qualityCallback = null;
    private SnapshotCallback snapshotCallback = null;
    private long snapshotId;

    /**
     * 截图
     */
    public void snapshot(Context context, long userId, SnapshotCallback callback) {
        if (snapshotUtil == null) {
            snapshotUtil = new SnapshotUtil(context);
        }
        this.snapshotCallback = callback;
        snapshotId = userId;
    }

    public void Quality(QualityCallback callback) {
        this.qualityCallback = callback;
    }

    public void setUpMediaEngine(Context context, String id, EngineConfigInfo engineConfigInfo) {
        IMediaEngine mediaEngine = null;
        IMediaView mediaView = null;
        if (engineConfigInfo == null || engineConfigInfo.getMediaType() == null) {
            return;
        }
        switch (engineConfigInfo.getMediaType()) {
            case MediaSAgora_VALUE: {
                mediaEngine = new AgoraMediaEngine(context);
                mediaView = new AgoraViewManager(context, Long.parseLong(id));
                break;
            }
            case MediaSHisrtc_VALUE: {
                mediaEngine = new HisrtcMediaEngine(context);
                mediaView = new HisrtcViewManager(context, Long.parseLong(id));
                break;
            }
        }

        if (mediaEngine != null) {
            if (MediaEngineHolder.Instance().getMediaEngine() != null) {
                MediaEngineHolder.Instance().getMediaEngine().destroy();
            }
            MediaEngineHolder.Instance().mediaEngine = mediaEngine;
            MediaEngineHolder.Instance().mediaEngine.init(engineConfigInfo);
        }
        if (mediaView != null) {
            MediaViewHolder.Instance().setMediaView(mediaView);
            MediaViewHolder.Instance().getMediaView().init();
        }
        SingletonHolder._instance.setContext(context.getApplicationContext());
    }
}
