package com.hiar.ar110.data;

import android.os.Parcel;
import android.os.Parcelable;

public class JQContent implements Parcelable{
    public BjjlData[]  bjjl;
    public GxrData[]  gxr;
    public positionData[]  position;
    public personData person;
    public String handleStatus;

    protected JQContent(Parcel in) {
        bjjl = in.createTypedArray(BjjlData.CREATOR);
        gxr = in.createTypedArray(GxrData.CREATOR);
        position = in.createTypedArray(positionData.CREATOR);
        person = in.readParcelable(personData.class.getClassLoader());
        handleStatus = "未处理";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(bjjl, flags);
        dest.writeTypedArray(gxr, flags);
        dest.writeTypedArray(position, flags);
        dest.writeParcelable(person, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JQContent> CREATOR = new Creator<JQContent>() {
        @Override
        public JQContent createFromParcel(Parcel in) {
            return new JQContent(in);
        }

        @Override
        public JQContent[] newArray(int size) {
            return new JQContent[size];
        }
    };

//    public ArrayList<KeyValue> getBjjl(int index) {
//        ArrayList<KeyValue> bjjlList = new ArrayList<>();
//
//        if(bjjl == null) {
//            return null;
//        }
//
//        if(bjjl.length == 0) {
//            return null;
//        }
//
//        if(index >= bjjl.length) {
//            return null;
//        }
//
//        bjjlList.addAll(bjjl[index].getContent());
//
//        return bjjlList;
//    }
//
//    public ArrayList<KeyValue> getGxr(int index) {
//        ArrayList<KeyValue> gxrList = new ArrayList<>();
//
//        if(gxr == null) {
//            return null;
//        }
//
//        if(gxr.length == 0) {
//            return null;
//        }
//
//        if(index >= gxr.length) {
//            return null;
//        }
//
//        gxrList.addAll(gxr[index].getContent());
//        return gxrList;
//    }
//
//    public ArrayList<KeyValue> getPosition(int index) {
//        ArrayList<KeyValue> positionList = new ArrayList<>();
//
//        if(position == null) {
//            return null;
//        }
//
//        if(position.length == 0) {
//            return null;
//        }
//
//        if(index >= position.length) {
//            return null;
//        }
//
//        positionList.addAll(position[index].getContent());
//        return positionList;
//    }
//
//    public ArrayList<KeyValue> getPerson() {
//        if(person != null) {
//            return person.getContent();
//        }
//        return null;
//    }
}
