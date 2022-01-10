package com.example.mediaengine.interfaces;

import android.content.Context;
import android.graphics.SurfaceTexture;

/**
 * 视频源接口，宿主或插件都可以实现 ICameraEngine 提供给 HiLeia 调用
 */
public interface ICameraEngine {
    interface OnNewFrameListener {
        void onNewFrame(byte[] data, int width, int height, int rotation);

        void onTextureFrame(SurfaceTexture surfaceTexture, float[] matrix, int width, int height, int rotation);

        void onError(int error);
    }

    interface FocusCallback {
        void onComplete(boolean result, String reason);
    }

    int openCamera(boolean isFront, int qualityLevel, SurfaceTexture surfaceTexture);

    int closeCamera();

    int switchCamera(int qualityLevel, SurfaceTexture mSurfaceTexture);

    int getPreViewWidth();

    int getPreViewHeight();

    String getInfo();

    boolean inUse();

    void setOrientation(int rotation);

    void setOnNewFrameListener(OnNewFrameListener onNewFrame);

    boolean isFrontCamera();

    void handleZoom(float ratio);

    void handleFocusMetering(int centerX, int centerY, FocusCallback callback);

    boolean switchFlashLight(Context context);

    void closeFlashLight(Context context);
}
