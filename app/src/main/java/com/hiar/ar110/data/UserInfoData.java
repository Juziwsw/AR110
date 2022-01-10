package com.hiar.ar110.data;
import android.os.Parcel;
import android.os.Parcelable;

import cn.com.cybertech.models.User;

public class UserInfoData implements Parcelable {
    public String account;        //账号
    public String name;           //姓名
    public String idcard;         //身份证
    public String sex;            //性别
    public String phone;          //电话号码
    public String email;          //email
    public String avatarUrl;     //
    public String deptId;        //部门ID
    public String position;       //职位
    public String uuid;           //uuid

    public UserInfoData() {

    }

    public UserInfoData(UserInfoData user) {
        account = user.account;
        name = user.name;
        idcard = user.idcard;
        sex = user.sex;
        phone = user.phone;
        email = user.email;
        avatarUrl = user.avatarUrl;
        deptId = user.deptId;
        position = user.position;
        uuid = user.uuid;
    }

    public UserInfoData(User user) {
        account = user.getAccount();
        name = user.getName();
        idcard = user.getIdCard();
        sex = user.getSex();
        phone = user.getPhone();
        email = user.getEmail();
        avatarUrl = user.getAvatarUrl();
        deptId = user.getDeptId();
        position = user.getPosition();
        uuid = String.valueOf(user.getUid());
    }

    protected UserInfoData(Parcel in) {
        account = in.readString();
        name = in.readString();
        idcard = in.readString();
        sex = in.readString();
        phone = in.readString();
        email = in.readString();
        avatarUrl = in.readString();
        deptId = in.readString();
        position = in.readString();
        uuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account);
        dest.writeString(name);
        dest.writeString(idcard);
        dest.writeString(sex);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(avatarUrl);
        dest.writeString(deptId);
        dest.writeString(position);
        dest.writeString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfoData> CREATOR = new Creator<UserInfoData>() {
        @Override
        public UserInfoData createFromParcel(Parcel in) {
            return new UserInfoData(in);
        }

        @Override
        public UserInfoData[] newArray(int size) {
            return new UserInfoData[size];
        }
    };
}
