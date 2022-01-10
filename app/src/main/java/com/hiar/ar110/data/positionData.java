package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class positionData implements Parcelable {
    public String dhaddr;    //定位位置名称
    public String dhdwjd;    //定位位置经度
    public String dhdwwd;    //定位位置维度
    public String dhjl;      //定位距离
    public String gxdw;      //管辖单位

    protected positionData(Parcel in) {
        dhaddr = in.readString();
        dhdwjd = in.readString();
        dhdwwd = in.readString();
        dhjl = in.readString();
        gxdw = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dhaddr);
        dest.writeString(dhdwjd);
        dest.writeString(dhdwwd);
        dest.writeString(dhjl);
        dest.writeString(gxdw);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<positionData> CREATOR = new Creator<positionData>() {
        @Override
        public positionData createFromParcel(Parcel in) {
            return new positionData(in);
        }

        @Override
        public positionData[] newArray(int size) {
            return new positionData[size];
        }
    };

//    public ArrayList<KeyValue> getContent() {
//        ArrayList<KeyValue> mList = new ArrayList<>();
//        mList.add(new KeyValue("定位位置名称",dhaddr));
//        mList.add(new KeyValue("定位位置经度",dhdwjd));
//        mList.add(new KeyValue("定位位置维度",dhdwwd));
//        mList.add(new KeyValue("定位距离",dhjl));
//        mList.add(new KeyValue("管辖单位",gxdw));
//        return mList;
//    }
}
