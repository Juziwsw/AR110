package com.hiar.ar110.data.people;

import com.hiar.ar110.data.FaceInfoList;

public class FaceCompareBaseInfo {
    public int id;
    public String jjdbh;
    public String cjdbh;
    public String cjdbh2;
    public String deviceId;
    public String name;
    public String fileHash;
    public double longitude;
    public double latitude;
    public String altitude;
    public String gpsTime;
    public String speed;
    public String imei;
    public String imsi;
    public String bearing;
    public String lastLocation;
    public String jybh;
    public String jygh;
    public String jyxm;
    public String faceName;
    public String faceUrl;
    public String cardId;
    public int sex;
    public int age;
    public String labelName;
    public int labelCode;
    public String similarity;
    public String createTime;
    public String faceBase64;   //人脸的base64
    public String localFaceBase64;   //人脸库中头像的base64

    public String verificationNumber;
    public String phone;
    public String fwcs;
    public String inTime;
    public String endTime;
    public String fwdz;
    public String dwdh;
    public String fzxm;
    public String fzphone;
    public String jzdz;

    public FaceCompareBaseInfo(){

    }

    public FaceCompareBaseInfo(FaceInfoList it){
        cardId =it.cardId;
        faceName =it.name;
        phone =it.phone;
        inTime =it.inTime;
        endTime =it.endTime;
        fwcs =it.fwcs;
        fwdz =it.fwdz;
        dwdh =it.dwdh;
        jzdz =it.jzdz;
        fzxm =it.fzxm;
        fzphone =it.fzphone;
        labelName =it.labelName;
        labelCode =it.labelCode;
        faceUrl =it.faceUrl;
        name=it.localFaceUrl;
        gpsTime=it.gpsTime;
        faceBase64=it.faceBase64;
        localFaceBase64=it.localFaceBase64;
        similarity=it.similarity;
    }
}




