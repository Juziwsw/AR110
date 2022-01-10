package com.hiar.ar110.data.patrol;

import com.hiar.ar110.data.LocationData;
import com.hiar.ar110.data.UserInfoData;

public class PatrolLocationReportData {
    public   String    patrolNumber;   //巡逻单号
    public   String    verificationNumber;   //核查任务单号
    public   String    deviceId;       //设备编号
    public   String    jybh;           //警员编号
    public   String    jygh;           //警员工号
    public   String    jyxm;           //警员姓名
    public   String    longitude;      //经度
    public   String    latitude;       //纬度
    public   String    altitude;       //高度
    public   String    bearing;        //角度
    public   String    gpsTime;        //GPS时间
    public   String    speed;          //当前速度
    public   String    xRotation;      //x轴旋转角
    public   String    yRotation;      //y轴旋转角
    public   String    zRotation;      //z轴旋转角
    public   String    deviceType;     //设备类型：摄像头
    public   String    manufactor;     //厂商：大华
    public   String    brand;          //品牌信息
    public   String    model;          //版型
    public   String    sdkInt;         //SDK版本
    public   String    imei;           //手机imei
    public   String    imsi;           //手机imsi，sim卡信息
    public   String    cid;            //
    public   String    lac;            //
    public   String    lastLocation;   //上次的位置
    public UserInfoData user;

    public PatrolLocationReportData(LocationData locData, UserInfoData policeUser) {
        lastLocation = "";
        bearing = locData.bearing;
        latitude = locData.latitude;
        longitude = locData.longitude;
        altitude = locData.altitude;
        gpsTime = locData.gps_time;
        speed = locData.speed;
        deviceType = "";
        brand = locData.brand;
        model = locData.model;
        sdkInt = locData.sdk_int;
        imei = locData.imei;
        imsi = locData.imsi;
        deviceId = imei;
        cid = locData.cid;
        lac = locData.lac;
        lastLocation = locData.last_location;
        if(null != policeUser) {
            jybh = policeUser.account;
            jyxm = policeUser.name;
        }
        user = policeUser;
    }
}
