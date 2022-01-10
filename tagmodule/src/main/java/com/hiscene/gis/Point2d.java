package com.hiscene.gis;


public class Point2d {
    public double x = 0;
    public double y = 0;

    public Point2d() {
    }

    public Point2d(double x ,double y) {
        this.x = x;
        this.y = y;
    }

    public double[] toArray(){
        return new double[]{x,y};
    }

    public void fromArray(double[] arr){
        this.x = arr[0];
        this.y = arr[1];
    }
}