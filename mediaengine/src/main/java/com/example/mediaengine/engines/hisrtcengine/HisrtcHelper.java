package com.example.mediaengine.engines.hisrtcengine;

import com.hiscene.cctalk.cloudrtc.CloudRtcService;

/**
 * Created by hujun on 2018/11/6.
 */

public class HisrtcHelper {
    private CloudRtcService mRtcEngine;
    private static HisrtcHelper instance;

    private HisrtcHelper() {
    }

    public synchronized static HisrtcHelper getInstance() {
        if (instance == null) {
            instance = new HisrtcHelper();
        }
        return instance;
    }

    public CloudRtcService getRtcEngine() {
        return mRtcEngine;
    }

    public void setRtcEngine(CloudRtcService mRtcEngine) {
        this.mRtcEngine = mRtcEngine;
    }
}
