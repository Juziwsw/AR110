package com.example.mediaengine.engines.hisrtcengine;

import android.content.Context;
import android.util.LongSparseArray;
import android.view.View;

import com.example.mediaengine.entity.VideoView;
import com.example.mediaengine.interfaces.IMediaView;
import com.hileia.common.utils.XLog;
import com.hiscene.cctalk.cloudrtc.CloudRtcService;
import com.hiscene.cctalk.video.VideoCanvas;

import org.hisav.videoengine.ViEAndroidGLES20;

/**
 * Created by hujun on 2018/11/6.
 */
public class HisrtcViewManager implements IMediaView {
    private static final String TAG = "HisrtcViewManager";
    private Context mContext;
    private LongSparseArray<View> rendererMap;
    private long mUserId;
    private static final long FULL_INDEX = -1;
    private HisrtcModel model;

    public HisrtcViewManager(Context mContext, long userId) {
        this.mContext = mContext;
        this.model = new HisrtcModel();
        this.mUserId = userId;
    }

    @Override
    public void init() {
        this.rendererMap = new LongSparseArray<>();
    }

    private View initRender(long userId) {
        View view = rendererMap.get(userId);
        if (view == null) {
            XLog.i(TAG, "initRender: " + userId);
            if (userId == mUserId) {
                view = new ViEAndroidGLES20(mContext);
            } else {
                view = CloudRtcService.CreateRendererView(mContext, false);
            }
            view.setTag("" + userId);
            rendererMap.put(userId, view);
        }
        view.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public VideoView addVideoView(long userId) {
        XLog.i(TAG, "addVideoView : " + userId);
        View view = initRender(userId);
        if (userId == mUserId) {
            HisrtcHelper.getInstance().getRtcEngine().setupLocalVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        } else {
            HisrtcHelper.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        }
        return new VideoView(view, userId);
    }

    @Override
    public void removeVideoView(long userId) {
        XLog.i(TAG, "removeVideoView: %d", userId);
        View view = rendererMap.get(userId);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
        if (userId == mUserId) {
            HisrtcHelper.getInstance().getRtcEngine().setupLocalVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        } else {
            HisrtcHelper.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        }
    }

    @Override
    public VideoView enterFullView(long userId) {
        XLog.i(TAG, "enterFullScreen: " + userId);
        //判断是否有视频
        View view = initRender(FULL_INDEX);
        view.setVisibility(View.VISIBLE);

        if (userId == mUserId) {
            HisrtcHelper.getInstance().getRtcEngine().muteLocalVideoStream(false);
            HisrtcHelper.getInstance().getRtcEngine().setupLocalVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        } else {
            HisrtcHelper.getInstance().getRtcEngine().muteRemoteVideoStream(model.transferUserId(userId), false);
            HisrtcHelper.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
        }
        return new VideoView(view, FULL_INDEX);
    }

    @Override
    public void exitFullView() {
        XLog.i(TAG, "exitFullScreen");
        View view = rendererMap.get(FULL_INDEX);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void pauseVideoView(long userId) {
        XLog.i(TAG, "pausePreview " + userId);
        View view = rendererMap.get(userId);
        if (view == null) {
            XLog.e(TAG, "pauseVideoView on null surface");
            return;
        }
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
            if (userId == mUserId) {
//                HisrtcHelper.getInstance().getRtcEngine().muteLocalVideoStream(true);
            } else {
                HisrtcHelper.getInstance().getRtcEngine().muteRemoteVideoStream(model.transferUserId(userId), true);
            }
        }
    }

    @Override
    public void resumeVideoView(long userId) {
        XLog.i(TAG, "resumePreview id=%s,my id=%s", userId, mUserId);
        View view = rendererMap.get(userId);
        if (view == null) {
            XLog.e(TAG, "resumeVideoView on null surface");
            return;
        }
        if (view.getVisibility() != View.VISIBLE) {
            if (userId == mUserId) {
                HisrtcHelper.getInstance().getRtcEngine().muteLocalVideoStream(false);
                HisrtcHelper.getInstance().getRtcEngine().setupLocalVideo(new VideoCanvas(rendererMap.get(userId), VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
            } else {
                HisrtcHelper.getInstance().getRtcEngine().muteRemoteVideoStream(model.transferUserId(userId), false);
                HisrtcHelper.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(rendererMap.get(userId), VideoCanvas.RENDER_MODE_HIDDEN, model.transferUserId(userId)));
            }
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void removeAllView() {
        XLog.i(TAG, "removeAllView");
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
}
