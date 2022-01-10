package com.hiar.ar110.data.cop;

import android.os.Parcel;
import android.os.Parcelable;
import com.hiar.ar110.data.GxrData;

public class CopTaskRecord implements Parcelable {
    public int id;
    public String  cjdbh;     // 处警单编号
    public String  cjdbh2;    //出警单编号
    public String  jjdbh;     // 接警单编号
    public String  afdd;      // 案发地点
    public int     bjcs;      // 报警次数
    public String  bjdh;      // 报警电话
    public String  bjnr;      // 报警内容
    public String  bjsj;      // 报警时间
    public int     cjzt;      // 处警状态
    public int     cjzt2;      // 处警状态
    public String  bjrxm;    // 报警人姓名
    public String  sfzbh;    // 身份证编号
    public String  dhdwjd;    // 电话定位精度
    public String  dhdwwd;    // 电话地位维度
    public GxrData[]  gxr;
    public CopInfo[]  cops;   //受警员信息

    public CopTaskRecord(CopTaskRecord record) {
        id = record.id;
        cjdbh = record.cjdbh;
        cjdbh2 = record.cjdbh2;
        jjdbh = record.jjdbh;
        afdd = record.afdd;
        bjcs = record.bjcs;
        bjdh = record.bjdh;
        bjnr = record.bjnr;
        bjsj = record.bjsj;
        cjzt = record.cjzt;
        cjzt2 = record.cjzt2;
        bjrxm = record.bjrxm;
        sfzbh = record.sfzbh;
        dhdwjd = record.dhdwjd;
        dhdwwd = record.dhdwwd;
        int len = record.cops.length;
        if(len > 0) {
            cops = new CopInfo[len];
            for(int i=0; i<len; i++) {
                cops[i] = new CopInfo(record.cops[i]);
            }
        }

    }

    protected CopTaskRecord(Parcel in) {
        id = in.readInt();
        cjdbh = in.readString();
        cjdbh2 = in.readString();
        jjdbh = in.readString();
        afdd = in.readString();
        bjcs = in.readInt();
        bjdh = in.readString();
        bjnr = in.readString();
        bjsj = in.readString();
        cjzt = in.readInt();
        cjzt2 = in.readInt();
        bjrxm = in.readString();
        sfzbh = in.readString();
        dhdwjd = in.readString();
        dhdwwd = in.readString();
        gxr = in.createTypedArray(GxrData.CREATOR);
        cops = in.createTypedArray(CopInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(cjdbh);
        dest.writeString(cjdbh2);
        dest.writeString(jjdbh);
        dest.writeString(afdd);
        dest.writeInt(bjcs);
        dest.writeString(bjdh);
        dest.writeString(bjnr);
        dest.writeString(bjsj);
        dest.writeInt(cjzt);
        dest.writeInt(cjzt2);
        dest.writeString(bjrxm);
        dest.writeString(sfzbh);
        dest.writeString(dhdwjd);
        dest.writeString(dhdwwd);
        dest.writeTypedArray(gxr, flags);
        dest.writeTypedArray(cops, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CopTaskRecord> CREATOR = new Creator<CopTaskRecord>() {
        @Override
        public CopTaskRecord createFromParcel(Parcel in) {
            return new CopTaskRecord(in);
        }

        @Override
        public CopTaskRecord[] newArray(int size) {
            return new CopTaskRecord[size];
        }
    };
}

