package com.example.mediaengine.interfaces;

import android.content.Context;

import com.example.mediaengine.entity.FrameData;
import com.example.mediaengine.entity.MediaQualityData;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import java.io.File;

/**
 * Created by hujun on 2018/11/6.
 */

public abstract class AbstractMediaEngine {
    private static final String TAG = "AbstractMediaEngine";
    protected Relay<FrameData> mFrameListener;
    protected Relay<MediaQualityData> mQualityListener;
    public String LOG_PATH;

    public AbstractMediaEngine(Context context) {
        LOG_PATH = context.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + "log";//log保存目录
        this.mFrameListener = PublishRelay.create();
        this.mQualityListener = PublishRelay.create();
    }

}
