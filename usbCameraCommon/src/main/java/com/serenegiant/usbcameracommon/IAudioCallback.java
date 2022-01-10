package com.serenegiant.usbcameracommon;

import java.nio.ByteBuffer;

/**
 * description : TODO:类的作用
 * author : cuiqingchao
 * date : 2020/7/8 16:32
 */
public interface IAudioCallback {
    public void onFrame(final ByteBuffer frame, int buffInfoSize);
}
