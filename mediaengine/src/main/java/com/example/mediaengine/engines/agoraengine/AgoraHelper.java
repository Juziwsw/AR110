package com.example.mediaengine.engines.agoraengine;

import com.example.mediaengine.entity.FrameData;
import com.jakewharton.rxrelay2.Relay;

import io.agora.rtc.RtcEngine;

/**
 * Created by hujun on 2018/11/6.
 */

public class AgoraHelper {
    private RtcEngine mRtcEngine;
    private Relay<FrameData> mFrameListener;
    private static AgoraHelper instance;

    private AgoraHelper(){}

    public synchronized static AgoraHelper getInstance(){
        if (instance==null){
            instance = new AgoraHelper();
        }
        return instance;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public void setRtcEngine(RtcEngine mRtcEngine) {
        this.mRtcEngine = mRtcEngine;
    }

    public Relay<FrameData> getFrameListener() {
        return mFrameListener;
    }

    public void setFrameListener(Relay<FrameData> mFrameListener) {
        this.mFrameListener = mFrameListener;
    }
}
