package com.hiar.ar110.data;

public class FaceSendData {
    public String jjdbh;    //接警单编好
    public String cjdbh;    //处警单编号
    public String cjdbh2;   //出警单编号
    public String deviceId;  //设备编号
    public String jybh;      //警员编号
    public String jyxm;      //警员姓名
    public String imei;      //手机IMEI
    public String imsi;      //手机imsi
    public String longitude;   //经度
    public String latitude;    //维度
    public String altitude;    //海拔
    public String bearing;     //轴角度
    public String gpsTime;      //gps时间
    public String lastLocation;   //上次的位置
    public int recoType;        //识别类型 1-公司API， 2 明略海康
    public int maxResults;     //返回的最大结果数量，默认为1
    public String minSimilarity;   //最小相似度，取值0-1之间，默认为0.5，可精确到小数点后面两位
    public int faceType;    //0：全部  1： 常驻人口  2： 暂住人口  3：在逃人员

    public FaceIcon images[];
}
