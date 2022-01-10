package com.example.mediaengine.engines.agoraengine;

import android.content.Context;
import android.util.LongSparseArray;
import android.view.SurfaceView;
import android.view.View;

import com.example.mediaengine.entity.FrameData;
import com.example.mediaengine.entity.VideoView;
import com.example.mediaengine.interfaces.IMediaView;
import com.hileia.common.utils.XLog;
import com.jakewharton.rxrelay2.Relay;

import static io.agora.rtc.mediaio.MediaIO.BufferType.BYTE_ARRAY;
import static io.agora.rtc.mediaio.MediaIO.PixelFormat.I420;

/**
 * Created by hujun on 2018/11/6.
 */
public class AgoraViewManager implements IMediaView, AgoraRenderView.FrameListener {
    private static final String TAG = "AgoraViewManagerTAG";
    private Context mContext;
    private LongSparseArray<AgoraRenderView> rendererMap;
    private long mUserId;
    private static final long FULL_INDEX = -1;
    private AgoraModel model;

    public AgoraViewManager(Context mContext, long userId) {
        this.mContext = mContext;
        this.model = new AgoraModel();
        this.mUserId = userId;
    }

    @Override
    public void init() {
        this.rendererMap = new LongSparseArray<>();
    }

    private AgoraRenderView initRender(long userId) {
        AgoraRenderView view = rendererMap.get(userId);
        if (view == null) {
            view = new AgoraRenderView(mContext);
            view.setBufferType(BYTE_ARRAY);
            view.setPixelFormat(I420);
            view.setName("renderView-" + userId);
            view.setUserId(userId);
            view.setFrameListener(this);
            rendererMap.put(userId, view);
        }
        view.setZOrderMediaOverlay(true);
        view.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public VideoView addVideoView(long userId) {
        XLog.d(TAG, "addVideoView : " + userId);
        AgoraRenderView surfaceView = initRender(userId);
        if (userId == mUserId) {
            AgoraHelper.getInstance().getRtcEngine().setLocalVideoRenderer(surfaceView);
        } else {
            AgoraHelper.getInstance().getRtcEngine().setRemoteVideoRenderer(model.transferUserId(userId), surfaceView);
        }
        return new VideoView(surfaceView, userId);
    }

    @Override
    public void removeVideoView(long userId) {
        XLog.i(TAG, "removeVideoView: %s", userId);
        final SurfaceView renderView = rendererMap.get(userId);
        if (renderView != null) {
            renderView.setVisibility(View.GONE);
        }
    }

    @Override
    public VideoView enterFullView(long userId) {
        XLog.i(TAG, "enterFullScreen: " + userId);
        //判断是否有视频
        AgoraRenderView surfaceView = initRender(FULL_INDEX);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView.setVisibility(View.VISIBLE);
        surfaceView.setUserId(userId);
        if (userId == mUserId) {
            AgoraHelper.getInstance().getRtcEngine().startPreview();
            AgoraHelper.getInstance().getRtcEngine().setLocalVideoRenderer(surfaceView);
        } else {
            AgoraHelper.getInstance().getRtcEngine().muteRemoteVideoStream(model.transferUserId(userId), false);
            AgoraHelper.getInstance().getRtcEngine().setRemoteVideoRenderer(model.transferUserId(userId), surfaceView);
        }
        return new VideoView(rendererMap.get(FULL_INDEX), userId);
    }

    @Override
    public void exitFullView() {
        XLog.i(TAG, "exitFullScreen");
        SurfaceView fullView = rendererMap.get(FULL_INDEX);
        if (fullView != null) {
            fullView.setZOrderMediaOverlay(false);
            fullView.setVisibility(View.GONE);
        }
    }

    @Override
    public void pauseVideoView(long userId) {
        XLog.i(TAG, "pausePreview %d",userId);
        SurfaceView surfaceViewRender = rendererMap.get(userId);
        if (surfaceViewRender != null) {
            surfaceViewRender.setVisibility(View.GONE);
        }
        if (userId == mUserId) {
            AgoraHelper.getInstance().getRtcEngine().setLocalVideoRenderer(null);
        } else {
            AgoraHelper.getInstance().getRtcEngine().setRemoteVideoRenderer(model.transferUserId(userId), null);
        }
    }

    @Override
    public void resumeVideoView(long userId) {
        XLog.i(TAG, "resumePreview id=%d,my id=%d", userId, mUserId);
        if (userId == mUserId) {
            AgoraHelper.getInstance().getRtcEngine().startPreview();
        } else {
            AgoraHelper.getInstance().getRtcEngine().muteRemoteVideoStream(model.transferUserId(userId), false);
        }
        AgoraRenderView surfaceViewRender = rendererMap.get(userId);
        if (surfaceViewRender == null) {
            return;
        }
        surfaceViewRender.setVisibility(View.VISIBLE);
        if (userId == mUserId) {
            AgoraHelper.getInstance().getRtcEngine().setLocalVideoRenderer(surfaceViewRender);
        } else {
            AgoraHelper.getInstance().getRtcEngine().setRemoteVideoRenderer(model.transferUserId(userId), surfaceViewRender);
        }
    }

    @Override
    public void removeAllView() {
        rendererMap.clear();
    }

    @Override
    public void pauseAllView() {
        for (int i = 0; i < rendererMap.size(); i++) {
            long id = rendererMap.keyAt(i);
            pauseVideoView(id);
        }
    }

    @Override
    public void resumeAllView() {
        for (int i = 0; i < rendererMap.size(); i++) {
            long id = rendererMap.keyAt(i);
            resumeVideoView(id);
        }
    }

    @Override
    public void onFrame(long userId, byte[] data, int pixelFormat, int width, int height, int rotation, long time) {
        Relay<FrameData> mFrameListener = AgoraHelper.getInstance().getFrameListener();
        if (mFrameListener != null) {
            if (userId == 0) {
                userId = mUserId;
            }
            mFrameListener.accept(new FrameData(data, data.length, width, height, userId, time));
        }
    }
}
