package com.ocom.hanmafacepay.network.entity

import android.os.Parcel
import android.os.Parcelable


data class PayEvent(val amount: Int, val userId: String, val offline: Int,val tradeNo:String?=null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(userId)
        parcel.writeInt(offline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayEvent> {
        override fun createFromParcel(parcel: Parcel): PayEvent {
            return PayEvent(parcel)
        }

        override fun newArray(size: Int): Array<PayEvent?> {
            return arrayOfNulls(size)
        }
    }
}