package com.hiscene.gis;

public class KalmanFilter {
    /**Kalman Filter*/
    private double predict;
    private double current;
    private double Gauss = 0;
    private double KalmanGain = 0;
    private double pdelt = 4;
    private double mdelt = 3;
    private final static double Q = 0.0000001;
    private final static double R = 0.1;

    public double filter(double oldValue,double value){
        //(1)第一个估计值
        predict = oldValue;
        current = value;
        //(2)高斯噪声方差
        Gauss = Math.sqrt(pdelt * pdelt + mdelt * mdelt) + Q;
        //(3)估计方差
        KalmanGain = Math.sqrt((Gauss * Gauss)/(Gauss * Gauss + pdelt * pdelt)) + R;
        //(4)估计值
        double estimate = (KalmanGain * (current - predict) + predict);
        //(5)新的估计方差
        mdelt = Math.sqrt((1f-KalmanGain) * Gauss * Gauss);

        return estimate;
    }

}
