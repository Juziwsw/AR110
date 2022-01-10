package com.example.mediaengine.engines.hisrtcengine;

import com.hileia.common.utils.XLog;
import com.hiscene.cctalk.video.VideoCaptureExternal;
import com.hiscene.cctalk.video.VideoFrameConsumer;

/**
 * author: liwf
 * date: 2020/8/7 19:16
 */
public class HisVideoSource implements VideoCaptureExternal {
    private final String TAG = getClass().getSimpleName();
    private VideoFrameConsumer mFrameConsumer;

    @Override
    public int getBufferType() {
        XLog.i(TAG, "getBufferType");
        return 0;
    }

    @Override
    public boolean onInitialize(VideoFrameConsumer videoFrameConsumer) {
        XLog.i(TAG, "onInitialize");
        mFrameConsumer = videoFrameConsumer;
        return true;
    }

    @Override
    public boolean onStart() {
        XLog.i(TAG, "onStart");
        return true;
    }

    @Override
    public void onStop() {
        XLog.i(TAG, "onStop");
    }

    @Override
    public void onDispose() {
        XLog.i(TAG, "onDispose");
    }

    public void inputVideoFrame(byte[] data, int width, int height, long timestamp) {
        if (mFrameConsumer != null) {
            mFrameConsumer.consumeByteArrayFrame(data, width, height, timestamp);
        }
    }
}
