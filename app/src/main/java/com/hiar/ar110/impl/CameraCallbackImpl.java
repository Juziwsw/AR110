package com.hiar.ar110.impl;

import android.util.Log;

import com.serenegiant.usbcameracommon.UVCCameraHandler;

/**
 * author: liwf
 * date: 2021/4/6 17:28
 */
public class CameraCallbackImpl implements UVCCameraHandler.CameraCallback {
    @Override
    public void onOpen() {
        Log.d("cam_det", "onOpen");
    }

    @Override
    public void onClose() {
        Log.d("cam_det", "onClose");
    }

    @Override
    public void onStartPreview() {
        Log.d("cam_det", "onStartPreview");
    }

    @Override
    public void onStopPreview() {
        Log.d("cam_det", "onStopPreview");
    }

    @Override
    public void onStartRecording() {
        Log.d("cam_det", "onStartRecording");
    }

    @Override
    public void onStopRecording() {
        Log.d("cam_det", "onStopRecording");
    }

    @Override
    public void onError(Exception e) {
        Log.d("cam_det", "onError");
    }
}
