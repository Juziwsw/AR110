package com.hiar.ar110.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Created by Administrator on 2016-09-13.
 */
public class GestureLayout extends ConstraintLayout implements GestureDetector.OnGestureListener {
    private
    int verticalMinDistance = 50;
    private int minVelocity = 60;

    public interface OnMyGestrueListener {
        public void slideLeft();
        public void slideRight();
    }


    private GestureDetector gestureDetector;
    Context context;
    private OnMyGestrueListener  myLayoutCallBack;

    public void setCallBack(OnMyGestrueListener listener) {
        myLayoutCallBack = listener;
    }

    public GestureLayout(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, this);
        this.context = context;
    }

    public GestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("pingan", "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("pingan", "onFling");

        if (e1.getX()
                - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
            Log.d("pingan", "向左手势");
            if(null != myLayoutCallBack) {
                myLayoutCallBack.slideLeft();
            }
        } else if ((e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity)) {
            Log.d("pingan", "向右手势");
            if(null != myLayoutCallBack) {
                myLayoutCallBack.slideRight();
            }
        }
        return true;
    }
}