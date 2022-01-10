package com.hiar.ar110.data;

import com.hiar.ar110.util.Util;

import java.util.Random;

/**
 * description : TODO:类的作用
 * author : cuiqingchao
 * date : 2020/7/28 16:04
 */
public class TargetInfo {
    public String name;
    public String libFaceUrl;
    public String idcard;
    public long faceID;
    public String mConf;
    public String faceUrl;
    public String capureTime;
    public String mCriminalType;
    public int mLabelCode;

    public TargetInfo(String peoname, String url, String id,
                      long fID, String conf, String face, String capTime, String criminal, int labelCode) {

        mLabelCode = labelCode;
        /*int max=5, min=0;
        int ran2 = (int) (Math.random()*(max-min)+min);
        mLabelCode = ran2;*/
        if(mLabelCode == 0) {
            int num = Util.findChinessNum(peoname);
            String fakeName = "";
            for(int i=0; i<num; i++) {
                fakeName += "x";
            }
            name = fakeName;
            libFaceUrl = url;
            int len = id.length();
            idcard = "";
            for(int i=0; i<len; i++) {
                idcard += "x";
            }
            faceID = fID;
            faceUrl = face;
            mConf = conf;
            capureTime = capTime;
            mCriminalType = "xxxxxxxx";

        } else {
            name = peoname;
            libFaceUrl = url;
            idcard = id;
            faceID = fID;
            faceUrl = face;
            mConf = conf;
            capureTime = capTime;
            mCriminalType = criminal;
        }
    }
}
