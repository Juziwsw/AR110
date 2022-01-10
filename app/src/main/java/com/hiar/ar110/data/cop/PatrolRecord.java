package com.hiar.ar110.data.cop;

import android.os.Parcel;
import android.os.Parcelable;

public class PatrolRecord implements Parcelable {
    public int id;
    public String number;
    public String startTime;
    public String endTime;
    public String context;
    public String account;
    public int status;
    public String createTime;
    public String updateTime;

    protected PatrolRecord(Parcel in) {
        id = in.readInt();
        number = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        context = in.readString();
        account = in.readString();
        status = in.readInt();
        createTime = in.readString();
        updateTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(number);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(context);
        dest.writeString(account);
        dest.writeInt(status);
        dest.writeString(createTime);
        dest.writeString(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PatrolRecord> CREATOR = new Creator<PatrolRecord>() {
        @Override
        public PatrolRecord createFromParcel(Parcel in) {
            return new PatrolRecord(in);
        }

        @Override
        public PatrolRecord[] newArray(int size) {
            return new PatrolRecord[size];
        }
    };
}