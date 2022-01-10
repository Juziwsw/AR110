package com.hiscene.armap;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class LocationManager implements AMapLocationListener {
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private LocationChangeListener mLocationChangeListener = null;
    private double[] mLonlat = {0,0,0};

    private static LocationManager sInstance = null;
    public static LocationManager getInstance() {
        if (sInstance == null) {
            sInstance = new LocationManager();
        }
        return sInstance;
    }

    public void init(Context context) {
        //初始化定位
        mLocationClient = new AMapLocationClient(context);
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化aMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为aMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(1000);
        mLocationClient.setLocationOption(mLocationOption);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);

        //开启缓存机制
        mLocationOption.setLocationCacheEnable(true);
    }

    //启动定位
    public void start() {
        mLocationClient.startLocation();
    }

    //停止定位
    public void stop() {
        mLocationClient.stopLocation();
    }

    //获取经纬度信息
    public double[] getLonlat() {
        return mLonlat;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mLonlat[0] = aMapLocation.getLongitude();
                mLonlat[1] = aMapLocation.getLatitude();
                if(mLocationChangeListener != null){
                    mLocationChangeListener.onLocationChanged(mLonlat[0],mLonlat[1]);
                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }


    public void setLocationChangeListener(LocationChangeListener listener){
        mLocationChangeListener = listener;
    }

    public interface LocationChangeListener {
        void onLocationChanged(double longitude,double latitude);
    }
}

