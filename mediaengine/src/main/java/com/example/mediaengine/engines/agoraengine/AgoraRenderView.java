package com.example.mediaengine.engines.agoraengine;

import android.content.Context;
import android.view.SurfaceHolder;

import com.hileia.common.utils.XLog;

import java.nio.ByteBuffer;

import io.agora.rtc.mediaio.AgoraSurfaceView;

/**
 * Created by hujun on 2018/11/7.
 */

public class AgoraRenderView extends AgoraSurfaceView {
    private static final String TAG = "AgoraRenderViewTAG";
    private String name;
    private long userId;
    private FrameListener frameListener;

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setFrameListener(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public AgoraRenderView(Context context) {
        super(context);
    }

    public void setName(String name){
        this.name = name;
    }


    @Override
    public void consumeByteArrayFrame(byte[] data, int pixelFormat, int width, int height, int rotation, long ts) {
        super.consumeByteArrayFrame(data, pixelFormat, width, height, rotation, ts);
        if (frameListener != null) {
            frameListener.onFrame(userId,data,pixelFormat,width,height,rotation,ts);
        }
    }

    @Override
    public void consumeByteBufferFrame(ByteBuffer buffer, int format, int width, int height, int rotation, long ts) {
        super.consumeByteBufferFrame(buffer, format, width, height, rotation, ts);
        XLog.d(TAG, "consumeByteBufferFrame");
    }

    @Override
    public void consumeTextureFrame(int texId, int pixelFormat, int width, int height, int rotation, long ts, float[] matrix) {
        super.consumeTextureFrame(texId, pixelFormat, width, height, rotation, ts, matrix);
        XLog.d(TAG, "consumeTextureFrame");
    }

    //byte[] data, int len, int width, int height, long userId, long time
    interface FrameListener{
        void onFrame(long userId, byte[] data, int pixelFormat, int width, int height, int rotation, long time);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        XLog.d(TAG, "surfaceChanged name=%s, w=%s,h=%s",name, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        XLog.d(TAG, "surfaceDestroyed name=%s",name);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        XLog.d(TAG, "surfaceCreated name=%s",name);
    }
}
