package com.hiar.ar110.data.cop;

import android.os.Parcel;
import android.os.Parcelable;

public class CopInfo implements Parcelable {
    public int id;                 // id
    public String copNum;          // 警员编号
    public String jygh;            // 警员工号
    public String name;            // 姓名
    public int sex;                // 性别  1. 男性 2. 女性
    public int sf;                 // 身份：1.民警 2.协警
    public String bm;              // 部门
    public String position;        // 职位(1.局领导,2.所长,3.副所长,4.警长,5.)
    public String mobile;          // 手机号
    public int status;             // 警员状态:1 待命，2、出警，3、巡逻，4、下线
    public String  createTime;     //创建时间
    public String deptId;          //部门ID

    protected CopInfo(CopInfo in) {
        id = in.id;
        copNum = in.copNum;
        jygh = in.jygh;
        name = in.name;
        sex = in.sex;
        sf = in.sf;
        bm = in.bm;
        position = in.position;
        mobile = in.mobile;
        status = in.status;
        createTime = in.createTime;
        deptId = in.deptId;
    }

    protected CopInfo(Parcel in) {
        id = in.readInt();
        copNum = in.readString();
        jygh = in.readString();
        name = in.readString();
        sex = in.readInt();
        sf = in.readInt();
        bm = in.readString();
        position = in.readString();
        mobile = in.readString();
        status = in.readInt();
        createTime = in.readString();
        deptId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(copNum);
        dest.writeString(jygh);
        dest.writeString(name);
        dest.writeInt(sex);
        dest.writeInt(sf);
        dest.writeString(bm);
        dest.writeString(position);
        dest.writeString(mobile);
        dest.writeInt(status);
        dest.writeString(createTime);
        dest.writeString(deptId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CopInfo> CREATOR = new Creator<CopInfo>() {
        @Override
        public CopInfo createFromParcel(Parcel in) {
            return new CopInfo(in);
        }

        @Override
        public CopInfo[] newArray(int size) {
            return new CopInfo[size];
        }
    };
}
