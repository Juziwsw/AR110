package com.example.mediaengine.entity;

import androidx.core.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * @author hujun
 * @date 18/6/27
 */

public class VideoView implements View.OnTouchListener {
    public View view;
    public long id;
    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    public VideoView(View view, long id) {
        this.view = view;
        this.view.setOnTouchListener(this);
        this.id = id;
    }

    public void setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        this.mScaleGestureDetector = scaleGestureDetector;
    }

    public void setGestureDetector(GestureDetectorCompat gestureDetector) {
        this.mGestureDetector = gestureDetector;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean res = mScaleGestureDetector.onTouchEvent(event);
        if (!mScaleGestureDetector.isInProgress()) {
            res = mGestureDetector.onTouchEvent(event);
        }
        return true;
    }
}
