package com.example.mediaengine.engines.agoraengine;

import com.hileia.common.utils.XLog;

import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;

/**
 * 实时通讯过程中，Agora SDK 通常会启动默认的视频输入设备，即内置的摄像头，进行视频推流。
 * 使用 IVideoSource 接口可以自定义视频源。通过调用 setVideoSource 接口，可以改变并控制默认的视频输入设备，
 * 再将自定义的视频源发送给 Agora Media Engine，让 Media Engine 进行其它视频处理，如过滤视频、将视频发布到 RTC 链接等。
 *
 * @author: liwenfei.
 * data: 2018/11/19 13:48.
 */
public class VideoSource implements IVideoSource {
    private static final String TAG = "VideoSourceTAG";
    IVideoFrameConsumer mConsumer;
    boolean mHasStarted;

    /**
     * 获取 Buffer 类型，有三种类型  BufferType.TEXTURE ，BufferType.BYTE_BUFFER，BufferType.BYTE_ARRAY
     * Media Engine 在初始化的时候，会调用这个方法来查询该视频源所使用的 Buffer 类型。开发者必须指定且只能指定一种 Buffer 类型并通过返回值告诉 Media Engine。
     * 若切换VideoSource的类型，必须重新创建另一个实例
     * @return
     */
    @Override
    public int getBufferType() {
        return MediaIO.BufferType.BYTE_ARRAY.intValue();
    }

    /**
     * IVideoFrameConsumer 接口。该接口支持接收三种 Buffer 类型的视频帧数据：ByteBuffer、ByteArray 和 Texture。请调用 getBufferType 方法指定 Buffer 类型。
     * Media Engine 在初始化视频源的时候会回调此方法。开发者可以在这个方法中做一些准备工作，例如打开 Camera，或者初始化视频源，并通过输入 true 或 false，以告知 Media Engine 自定义的视频源是否已经准备好。
     * @param consumer 是由SDK创建的，在video source生命周期中注意保存它的引用。Media Engine 传递给开发者的一个 IVideoFrameConsumer 对象。开发者需要保存该对象，并在视频源启动后，通过这个对象把视频帧输入给 Media Engine
     * @return
     */
    @Override
    public boolean onInitialize(IVideoFrameConsumer consumer) {
        XLog.d(TAG, "onInitialize");
        mConsumer = consumer;
        return true;
    }

    /**
     * 启动视频源
     * Media Engine 在启动视频源时会回调这个方法。开发者可以在该方法中启动视频帧捕捉。开发者需要输入 true 或 false，以告知 Media Engine 自定义的视频源是否开启成功。
     * @return
     */
    @Override
    public boolean onStart() {
        XLog.d(TAG, "onStart");
        mHasStarted = true;
        return mHasStarted;
    }

    /**
     * 停止视频源
     * Media Engine 在停止视频源的时候会回调这个方法。开发者可以在这个方法中停止视频的采集。Media Engine 通过这个回调通知开发者，IVideoFrameConsumer 的帧输入开关即将关闭，之后输入的视频帧都会被丢弃。
     */
    @Override
    public void onStop() {
        XLog.d(TAG, "onStop");
        mHasStarted = false;
    }

    /**
     * 释放视频源
     * Media Engine 通知开发者视频源即将失效，开发者可以在这个方法中关闭视频源设备。引擎会销毁 IVideoFrameConsumer 对象，开发者需要确保在此回调之后不再使用它。
     */
    @Override
    public void onDispose() {
        XLog.d(TAG, "onDispose");
        mConsumer = null;
    }

    /**
     * consumeByteArrayFrame 接收 ByteBuffer 类型的视频帧
     * @param data Byte Array 型的数据数据
     * param format 像素格式：I420 NV21 RGBA
     * @param width 视频帧的宽度
     * @param height 视频帧的高度
     * param rotation 视频帧顺时针旋转的角度。如果设置了旋转角度，媒体引擎会对图像进行旋转。你可以根据需要将角度值设为 0 度、90 度、180 度和 270 度，如果设置为其他数字，系统会自动忽略
     * @param timestamp 传入的视频帧的时间戳。开发者必须为每一个视频帧设置一个时间戳
     */
    public void inputVideoFrame(byte[] data, int len, int width, int height, int fmt, int mirror, long timestamp) {
        if (mHasStarted && mConsumer != null) {
            mConsumer.consumeByteArrayFrame(data, MediaIO.PixelFormat.NV21.intValue(), width, height, 0, timestamp);
        }
    }
}

