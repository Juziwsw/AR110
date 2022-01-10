package com.hiscene.gis;

public class Position {
    public PointLonLat lonLatPoint = new PointLonLat();			//经纬度坐标
    public Point2d screenPoint = new Point2d();					//点在中屏幕相对位置,单位是(像素/宽)
    public Point2d worldPoint = new Point2d();					//平面世界坐标
    public Point3d cameraPoint = new Point3d();					//3d相机坐标

    public Position() {
    }

    public double[] toArray(){
        return new double[]{lonLatPoint.lon,lonLatPoint.lon,lonLatPoint.height,screenPoint.x,screenPoint.y,worldPoint.x,worldPoint.y,cameraPoint.x,cameraPoint.y,cameraPoint.z};
    }

    public void fromArray(double[] arr){
        int index = 0;
        lonLatPoint.lon = arr[index++];
        lonLatPoint.lat = arr[index++];
        lonLatPoint.height = arr[index++];

        screenPoint.x = arr[index++];
        screenPoint.y = arr[index++];

        worldPoint.x = arr[index++];
        worldPoint.y = arr[index++];

        cameraPoint.x = arr[index++];
        cameraPoint.y = arr[index++];
        cameraPoint.z = arr[index++];
    }
}
