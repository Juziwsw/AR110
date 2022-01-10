package com.hiar.ar110.data.cop;

public class VideoInfoUploadData {
    public String jjdbh;     //接警单编号
    public String cjdbh;     //处警单编号
    public String cjdbh2;    //出警单编号
    public String deviceId;  //设备编号
    public String jybh;      //警员编号
    public String jyxm;      //警员姓名
    public String longitude;  //经度
    public String latitude;  //纬度
    public String altitude;  //高度
    public String bearing;   //角度
    public String gpsTime;   //GPS时间
    public String lastLocation; //上次的位置
    public UploadVideoInfo videoInfo[];
    public int id;    //记录主键号
    public String patrolNumber;
}
