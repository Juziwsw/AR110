package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class personData implements Parcelable {
    public String badd;    //报案地点
    public String name;    //姓名
    public String phone;   //电话
    public String photo;   //头像
    public String zjhm;    //证件号码
    public String[] bq;    //标签
    public String[] jwd;   //定位经纬度

    protected personData(Parcel in) {
        badd = in.readString();
        name = in.readString();
        phone = in.readString();
        photo = in.readString();
        zjhm = in.readString();
        bq = in.createStringArray();
        jwd = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(badd);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(photo);
        dest.writeString(zjhm);
        dest.writeStringArray(bq);
        dest.writeStringArray(jwd);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<personData> CREATOR = new Creator<personData>() {
        @Override
        public personData createFromParcel(Parcel in) {
            return new personData(in);
        }

        @Override
        public personData[] newArray(int size) {
            return new personData[size];
        }
    };

//    public ArrayList<KeyValue> getContent() {
//        ArrayList<KeyValue> mList = new ArrayList<>();
//        mList.add(new KeyValue("报案地点",badd));
//        mList.add(new KeyValue("姓名",name));
//        mList.add(new KeyValue("电话",phone));
//        mList.add(new KeyValue("头像",photo));
//        mList.add(new KeyValue("证件号码",zjhm));
//
//        if(bq != null && bq.length > 0) {
//            for(int i=0; i<bq.length; i++) {
//                mList.add(new KeyValue("标签",bq[i]));
//            }
//        }
//
//        if(jwd != null && jwd.length == 2) {
//            mList.add(new KeyValue("定位经度",jwd[0]));
//            mList.add(new KeyValue("定位维度",jwd[1]));
//        }
//
//        return mList;
//    }
}
