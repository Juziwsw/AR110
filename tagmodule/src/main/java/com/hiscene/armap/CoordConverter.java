package com.hiscene.armap;

import com.hiscene.gis.Point2d;
import com.hiscene.gis.PointLonLat;
import com.hiscene.gis.Position;

public class CoordConverter {
    private long owner = 0L;

    private static CoordConverter sInstance = null;
    public static CoordConverter getInstance() {
        if (sInstance == null) {
            sInstance = new CoordConverter();
        }
        return sInstance;
    }

    public CoordConverter() {
        owner = HiARVGISNative.CoordConverterConstructor();
    }

    /**
     * 设置相机内参
     *
     * @param owner           CoordConverter指针
     * @param height          相机高度
     * @param intrinsics      相机内参
     * @param intrinsic_count 内参个数
     */
    public void setCameraIntrinsics(double height, CameraIntrinsic intrinsic) {
        HiARVGISNative.setCameraIntrinsics(owner, height, intrinsic.toArray(), 1);
    }

    /**
     * 屏幕坐标转经纬度
     *
     * @param owner        CoordConverter指针
     * @param ptz          相机PTZ信息[Pan,Tilt,Zoom]
     * @param cameraLonLat 相机经纬度[lon,lat,height]
     * @param screen       tag屏幕信息[x,y]
     * @return tag位置信息
     */
    public Position screenPointTolonLat(CameraPTZ ptz, PointLonLat cameraLonLat, Point2d screen) {
        Position pos = new Position();
        pos.fromArray(HiARVGISNative.screenPointTolonLat(owner, ptz.toArray(), cameraLonLat.toArray(), screen.toArray()));
        return pos;
    }


    /**
     * 经纬度信息转屏幕坐标
     *
     * @param owner        CoordConverter指针
     * @param ptz          相机PTZ信息[Pan,Tilt,Zoom]
     * @param cameraLonLat 相机经纬度[lon,lat,height]
     * @param screen       tag经纬度信息[lon,lat,height]
     * @return tag位置信息
     */
    public Position lonLatPointToScreen(CameraPTZ ptz, PointLonLat cameraLonLat, PointLonLat tagLonLat) {
        Position pos = new Position();
        double[] arr = HiARVGISNative.lonLatPointToScreen(owner, ptz.toArray(), cameraLonLat.toArray(), tagLonLat.toArray());
        pos.fromArray(arr);
        return pos;
    }


    public class CameraPTZ{

        public double pan = 0;
        public double tilt = 0;
        public double zoom = 0;

        public CameraPTZ() {
        }

        public CameraPTZ(double pan ,double tilt ,double zoom) {
            this.pan = pan;
            this.tilt = tilt;
            this.zoom = zoom;
        }

        public double[] toArray(){
            return new double[]{pan,tilt,zoom};
        }

        public void fromArray(double[] arr){
            this.pan = arr[0];
            this.tilt = arr[1];
            this.zoom = arr[2];
        }
    }

    public class CameraIntrinsic{

        public double width = 0;              /// number of cols in pixels of image for calibration
        public double height = 0;             /// number of rows in pixels of image for calibration

        public double fx = 0;              /// focal length x
        public double fy = 0;              /// focal length y
        public double cx = 0;              /// principal point x
        public double cy = 0;              /// principal point y

        public double k1 = 0;              /// first radial distortion coefficient
        public double k2 = 0;              /// second radial distortion coefficient
        public double k3 = 0;              /// third radial distortion coefficient
        public double p1 = 0;              /// first tangential distortion coefficient
        public double p2 = 0;              /// second tangential distortion coefficient

        public double zoom = 0;            /// scale factor

        public double[] toArray(){
            return new double[]{width,height,fx,fy,cx,cy,k1,k2,k3,p1,p2,zoom};
        }
    }

}

