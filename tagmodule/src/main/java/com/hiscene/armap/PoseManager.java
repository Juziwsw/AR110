package com.hiscene.armap;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PoseManager implements SensorEventListener {
    private final static int MAX_SENSOR_QUEUE_LEN = 5;
    private SensorManager mSensorManager = null;
    private Sensor mSensorAcc = null;
    private Sensor mSensorMagnetic = null;
    private List<SensorCell> mAccs = new ArrayList<>(MAX_SENSOR_QUEUE_LEN);
    private List<SensorCell> mMags = new ArrayList<>(MAX_SENSOR_QUEUE_LEN);
    private static PoseManager sInstance = null;

    private class SensorCell{
        public float[] data = new float[3];
        public SensorCell(float[] values){
            data[0] = values[0];
            data[1] = values[1];
            data[2] = values[2];
        }
    }

    public static PoseManager getInstance() {
        if (sInstance == null) {
            sInstance = new PoseManager();
        }
        return sInstance;
    }

    public void init(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    public void start() {
        if (mSensorManager != null && mSensorAcc != null) {
            mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager != null && mSensorMagnetic != null) {
            mSensorManager.registerListener(this, mSensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }


    public float[] getOrientations() {
        float[] acc = new float[] {0,0,0};
        float[] mag = new float[] {0,0,0};
        float[] data = null;

        for(int i=0;i<MAX_SENSOR_QUEUE_LEN;i++){
            data = mAccs.get(i).data;
            acc[0] += data[0];
            acc[1] += data[1];
            acc[2] += data[2];

            data = mMags.get(i).data;
            mag[0] += data[0];
            mag[1] += data[1];
            mag[2] += data[2];
        }
        //cal sensor data mean value
        acc[0] /= MAX_SENSOR_QUEUE_LEN;
        acc[1] /= MAX_SENSOR_QUEUE_LEN;
        acc[2] /= MAX_SENSOR_QUEUE_LEN;

        mag[0] /= MAX_SENSOR_QUEUE_LEN;
        mag[1] /= MAX_SENSOR_QUEUE_LEN;
        mag[2] /= MAX_SENSOR_QUEUE_LEN;

        float[] rotationMatrix = new float[9];   //rotation matrix
        float[] orientations = new float[3];
        SensorManager.getRotationMatrix(rotationMatrix, null, acc, mag);
        SensorManager.getOrientation(rotationMatrix, orientations);
        orientations[0] = (float) Math.toDegrees(orientations[0]);
        orientations[1] = (float) Math.toDegrees(orientations[1]);
        orientations[2] = (float) Math.toDegrees(orientations[2]);
        Log.d("imu","imu show:   "+orientations[0] + ",   "+orientations[1] + ",    "+orientations[2]);
        return orientations;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if(mAccs.size() >= MAX_SENSOR_QUEUE_LEN){
                    mAccs.remove(0);
                }
                mAccs.add(new SensorCell(sensorEvent.values));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if(mMags.size() >= MAX_SENSOR_QUEUE_LEN){
                    mMags.remove(0);
                }
                mMags.add(new SensorCell(sensorEvent.values));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}



