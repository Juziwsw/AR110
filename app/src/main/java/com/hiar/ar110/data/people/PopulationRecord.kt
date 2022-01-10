package com.hiar.ar110.data.people

import android.os.Parcel
import android.os.Parcelable

/**
 * Author:wilson.chen
 * date：5/13/21
 * desc：人口核查数据
 */
data class PopulationRecord(
        var id: Int = 0,
        var number: String? = null,
        var startTime: String? = null,
        var endTime: String? = null,
        var context: String? = null,
        var account: String? = null,
        /**
         * 0-巡逻中，1-巡逻完成
         */
        var status: Int=0,
        var createTime: String? = null,
        var updateTime: String? = null,
        val name: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(number)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(context)
        parcel.writeString(account)
        parcel.writeInt(status)
        parcel.writeString(createTime)
        parcel.writeString(updateTime)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PopulationRecord> {
        override fun createFromParcel(parcel: Parcel): PopulationRecord {
            return PopulationRecord(parcel)
        }

        override fun newArray(size: Int): Array<PopulationRecord?> {
            return arrayOfNulls(size)
        }
    }

}