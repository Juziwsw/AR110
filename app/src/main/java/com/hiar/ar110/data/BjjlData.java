package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class BjjlData implements Parcelable {
    public String afdd;       //案发地点
    public String bjcs;       //报警次数
    public String bjdh;       //报警电话
    public String cjqk;       //处警情况
    public String dhdwjd;     //电话定位经度
    public String dhdwwd;     //电话定位纬度
    public String fkdwmc;     //反馈单位名称
    public String fkrxm;      //反馈人姓名
    public String jjdbh;      //接警单编号
    public String bjnr;       //报警内容
    public String bjsj;       //报警时间

    protected BjjlData(Parcel in) {
        afdd = in.readString();
        bjcs = in.readString();
        bjdh = in.readString();
        cjqk = in.readString();
        dhdwjd = in.readString();
        dhdwwd = in.readString();
        fkdwmc = in.readString();
        fkrxm = in.readString();
        jjdbh = in.readString();
        bjnr = in.readString();
        bjsj = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(afdd);
        dest.writeString(bjcs);
        dest.writeString(bjdh);
        dest.writeString(cjqk);
        dest.writeString(dhdwjd);
        dest.writeString(dhdwwd);
        dest.writeString(fkdwmc);
        dest.writeString(fkrxm);
        dest.writeString(jjdbh);
        dest.writeString(bjnr);
        dest.writeString(bjsj);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BjjlData> CREATOR = new Creator<BjjlData>() {
        @Override
        public BjjlData createFromParcel(Parcel in) {
            return new BjjlData(in);
        }

        @Override
        public BjjlData[] newArray(int size) {
            return new BjjlData[size];
        }
    };

//    public ArrayList<KeyValue> getContent() {
//        ArrayList<KeyValue> mList = new ArrayList<>();
//        mList.add(new KeyValue("案发地点",afdd));
//        mList.add(new KeyValue("报警次数",bjcs));
//        mList.add(new KeyValue("报警电话",bjdh));
//        mList.add(new KeyValue("处警情况",cjqk));
//        mList.add(new KeyValue("电话定位经度",dhdwjd));
//        mList.add(new KeyValue("电话定位纬度",dhdwwd));
//        mList.add(new KeyValue("反馈单位名称",fkdwmc));
//        mList.add(new KeyValue("反馈人姓名",fkrxm));
//        mList.add(new KeyValue("接警单编号",jjdbh));
//        mList.add(new KeyValue("报警内容",bjnr));
//        mList.add(new KeyValue("报警时间",bjsj));
//        return mList;
//    }
}
