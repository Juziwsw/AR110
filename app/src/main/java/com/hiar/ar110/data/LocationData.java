package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationData implements Parcelable {
    public String brand;        //品牌信息
    public String model;        //版型
    public String sdk_int;      //SDK版本
    public String imei;         //警务通imei
    public String imsi;         //手机IMSI，sim卡信息
    public String altitude;     //海拔
    public String bearing;      //轴方向
    public String latitude;     //维度
    public String longitude;    //经度
    public String gps_time;     //GPS时间
    public String speed;        //当前速度
    public String cid;          //gsm cell id/cdma base station identification number
    public String lac;          //gsm location area code/cdma network identification number
    public String address_query_url;
    public String coordinate_conversion_url;
    public String cell_location_url;
    public String last_location;    //上次的位置
    public String deviceId;

    public LocationData() {

    }

    public LocationData(LocationData src) {
        if(src == null) {
            return;
        }

        brand = src.brand;
        model = src.model;
        sdk_int = src.sdk_int;
        imei = src.imei;
        imsi = src.imsi;
        altitude = src.altitude;
        bearing = src.bearing;
        latitude = src.latitude;
        longitude = src.longitude;
        gps_time = src.gps_time;
        speed = src.speed;
        cid = src.cid;
        lac = src.lac;
        address_query_url = src.address_query_url;
        coordinate_conversion_url = src.coordinate_conversion_url;
        cell_location_url = src.cell_location_url;
        last_location = src.last_location;
        deviceId = imei;
    }

    protected LocationData(Parcel in) {
        brand = in.readString();
        model = in.readString();
        sdk_int = in.readString();
        imei = in.readString();
        imsi = in.readString();
        altitude = in.readString();
        bearing = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        gps_time = in.readString();
        speed = in.readString();
        cid = in.readString();
        lac = in.readString();
        address_query_url = in.readString();
        coordinate_conversion_url = in.readString();
        cell_location_url = in.readString();
        last_location = in.readString();
        deviceId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(brand);
        dest.writeString(model);
        dest.writeString(sdk_int);
        dest.writeString(imei);
        dest.writeString(imsi);
        dest.writeString(altitude);
        dest.writeString(bearing);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(gps_time);
        dest.writeString(speed);
        dest.writeString(cid);
        dest.writeString(lac);
        dest.writeString(address_query_url);
        dest.writeString(coordinate_conversion_url);
        dest.writeString(cell_location_url);
        dest.writeString(last_location);
        dest.writeString(deviceId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationData> CREATOR = new Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };
}
