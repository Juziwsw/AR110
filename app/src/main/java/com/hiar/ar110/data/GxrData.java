package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class GxrData implements Parcelable {
    public String relation;   //关系
    public String gxrxm;      //关系人姓名
    public String imageUrl;   //头像地址
    public String sfz;        //身份证

    protected GxrData(Parcel in) {
        relation = in.readString();
        gxrxm = in.readString();
        imageUrl = in.readString();
        sfz = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(relation);
        dest.writeString(gxrxm);
        dest.writeString(imageUrl);
        dest.writeString(sfz);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GxrData> CREATOR = new Creator<GxrData>() {
        @Override
        public GxrData createFromParcel(Parcel in) {
            return new GxrData(in);
        }

        @Override
        public GxrData[] newArray(int size) {
            return new GxrData[size];
        }
    };

//    public ArrayList<KeyValue> getContent() {
//        ArrayList<KeyValue> mList = new ArrayList<>();
//        mList.add(new KeyValue("关系",relation));
//        mList.add(new KeyValue("关系人姓名",gxrxm));
//        mList.add(new KeyValue("头像地址",imageUrl));
//        mList.add(new KeyValue("身份证",sfz));
//        return mList;
//    }
}
