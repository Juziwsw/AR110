package com.hiscene.gis;


public class PointLonLat {
    public double lon = 0;        //longitude,经度
    public double lat = 0;        //latitude,维度
    public double height = 0;        //altitude,高程

    public PointLonLat() {
    }

    public PointLonLat(double lon ,double lat ,double height) {
        this.lon = lon;
        this.lat = lat;
        this.height = height;
    }

    public double[] toArray(){
        return new double[]{lon,lat,height};
    }

    public void fromArray(double[] arr){
        this.lon = arr[0];
        this.lat = arr[1];
        this.height = arr[2];
    }
}