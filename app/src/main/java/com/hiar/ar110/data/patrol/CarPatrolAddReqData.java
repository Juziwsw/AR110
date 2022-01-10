package com.hiar.ar110.data.patrol;

import com.hiar.ar110.data.cop.PatrolRecord;
import com.hiar.ar110.data.LocationData;
import com.hiar.ar110.data.UserInfoData;
import com.hiar.ar110.util.Base64Util;

import java.io.File;

public class CarPatrolAddReqData {
    public String patrolNumber;
    public String deviceId;
    public String imei;
    public String imsi;
    public String longitude;
    public String latitude;
    public String altitude;
    public String bearing;
    public String gpsTime;
    public String lastLocation;
    public String jybh;
    public String jygh;
    public String jyxm;
    public CarImage images;

    public static class CarImage {
        public String carId;
        public String carImage;
        public String plateColor;
    }

    public CarPatrolAddReqData() {

    }

    public CarPatrolAddReqData(PatrolRecord record, LocationData loc, UserInfoData user, String imgName, String plateNum, String plateColor) {
        if(record != null) {
            patrolNumber = record.number;
        }

        if(loc != null) {
            deviceId = loc.deviceId;
            imei = loc.imei;
            imsi = loc.imsi;
            longitude = loc.longitude;
            latitude = loc.latitude;
            bearing = loc.bearing;
            altitude = loc.altitude;
            lastLocation = loc.last_location;
        }

        gpsTime = String.valueOf(System.currentTimeMillis());
        if(null != user) {
            jybh = user.account;
            jygh = user.account;
            jyxm = user.name;
        }

        images = new CarImage();
        File imgFile = new File(imgName);
        if(imgFile.exists()) {
            images.carImage = Base64Util.imageToBase64ByLocal(imgName);
        }

        images.carId = plateNum;
        images.plateColor = plateColor;
    }

}
