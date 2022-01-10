package com.hiar.ar110.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.util.Util;
import com.hiar.mybaselib.utils.AR110Log;

public class SensorUtil implements SensorEventListener {
    private static final String TAG = "SensorUtil";
    private static SensorUtil  mSensorUtil;
    private boolean mHasStarted = false;
    private SensorManager mSensorManager = null;
    private static Handler mHandler;
    private static boolean isNear = false;

    public static final int MSG_IS_NEAR = 21;
    public static final int MSG_IS_FAR = 22;
    public static final int MSG_CHECK_SERVICE_RUNNING = 23;

    private SensorUtil() {

    }

    public static SensorUtil  getInstance() {
        if(null != mSensorUtil) {
            return mSensorUtil;
        } else {
            mSensorUtil = new SensorUtil();
        }

        return mSensorUtil;
    }

    public static void start(Context context, Handler handler) {
        SensorUtil.getInstance().registerListener(context);
        mHandler = handler;
    }

    public static void stop() {
        SensorUtil.getInstance().unregisterListener();
        mHandler = null;
    }

    private void registerListener(Context context) {
        if (mHasStarted) {
            return;
        }

        mHasStarted = true;
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); // 获取距离传感器
        if (proximitySensor != null) { // 距离传感器存在时才执行
            mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL); //
        }
    }

    private void unregisterListener() {
        if (!mHasStarted || mSensorManager == null) {
            return;
        }
        mHasStarted = false;
        mSensorManager.unregisterListener(this);
    }

    private static float distance = 5.0f;
    private static float lightValue = 100.0f;
    private long mLastCloseTime = 0;
    private long mLastOpenTime = 0;

    public float getDistance() {
        return distance;
    }

    public float getLightValue() {
        return lightValue;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(event.values[0] == 0.0f) {
                if(mHandler != null) {
                    mHandler.removeMessages(SensorUtil.MSG_IS_NEAR);
                    mHandler.removeMessages(SensorUtil.MSG_IS_FAR);
                    mHandler.sendEmptyMessageDelayed(SensorUtil.MSG_IS_NEAR, 500);
                    isNear = true;
                }
            }

            if(event.values[0] == 5.0f) {
                if(mHandler != null) {
                    mHandler.removeMessages(SensorUtil.MSG_IS_FAR);
                    mHandler.removeMessages(SensorUtil.MSG_IS_NEAR);
                    mHandler.sendEmptyMessageDelayed(SensorUtil.MSG_IS_FAR, 500);
                    isNear = false;
                }
            }

            AR110Log.i("sensor", "proximitySensor dis="+event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
