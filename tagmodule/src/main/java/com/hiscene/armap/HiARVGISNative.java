package com.hiscene.armap;

public class HiARVGISNative {

    public static native String stringFromJNI();

    public static native long CoordConverterConstructor();

    public static native void CoordConverterDestructor(long owner);

    /**
     * 设置相机内参
     * @param owner             CoordConverter指针
     * @param height            相机高度
     * @param intrinsics        相机内参
     * @param intrinsic_count   内参个数
     */
    public static native void setCameraIntrinsics(long owner, double height, double intrinsics[],int intrinsic_count);

    /**
     * 屏幕坐标转经纬度
     * @param owner         CoordConverter指针
     * @param ptz           相机PTZ信息[Pan,Tilt,Zoom]
     * @param cameraLonLat  相机经纬度[lon,lat,height]
     * @param screen        tag屏幕信息[x,y]
     * @return              tag经纬度信息[lon,lat,height]
     */
    public static native double[] screenPointTolonLat(long owner, double[] ptz, double[] cameraLonLat, double[] screen);


    /**
     * 经纬度信息转屏幕坐标
     * @param owner         CoordConverter指针
     * @param ptz           相机PTZ信息[Pan,Tilt,Zoom]
     * @param cameraLonLat  相机经纬度[lon,lat,height]
     * @param screen        tag经纬度信息[lon,lat,height]
     * @return              tag屏幕信息[x,y]
     */
    public static native double[] lonLatPointToScreen(long owner, double[] ptz, double[] cameraLonLat, double[] tagLonLat);



}
