package com.hiar.ar110.data.audio;

import java.util.List;

/**
 * @author tangxucheng
 * @date 2021/6/17
 * Email: xucheng.tang@hiscene.com
 */
public class AudioListData {
    /**
     * {"sort":"desc","order":"id","total":2,"audioList":[{"id":4,"patrolNumber":"3320210617135756083386","jybh":"","jygh":"1debe0624","jyxm":"警员0624","name":"2021-06-17_14-32-54.mp3","deviceId":"b75a0b91debe0624","audioUrl":"group1/M00/00/EB/rBEAEGDK7DOAPn4DAABi8ssHwxE293.mp3","longitude":121.6241,"latitude":31.2105,"altitude":"0.0","gpsTime":"2021-06-17 14:32:55","speed":"","bearing":"","lastLocation":"","createTime":"2021-06-17 14:31:15"},{"id":3,"patrolNumber":"3320210617135756083386","jybh":"","jygh":"1debe0624","jyxm":"警员0624","name":"2021-06-17_14-32-45.mp3","deviceId":"b75a0b91debe0624","audioUrl":"group1/M00/00/EB/rBEAEGDK7DOAVBkTAADIhJWPPPc682.mp3","longitude":121.6241,"latitude":31.2105,"altitude":"0.0","gpsTime":"2021-06-17 14:32:45","speed":"","bearing":"","lastLocation":"","createTime":"2021-06-17 14:31:15"}],"audioUrl":"http://106.75.214.60:10177/","from":0,"limit":10}
     */
    public String sort;
    public String order;
    public Integer total;
    public List<AudioList> audioList;
    public String audioUrl;
    public Integer from;
    public Integer limit;
    
    public static class AudioList {
        public Integer id;
        public String patrolNumber;
        public String jybh;
        public String jygh;
        public String jyxm;
        public String name;
        public Integer isUpload;
        public String deviceId;
        public String audioUrl;
        public Double longitude;
        public Double latitude;
        public String altitude;
        public String gpsTime;
        public String speed;
        public String bearing;
        public String lastLocation;
        public String createTime;
    }
    
    
}
