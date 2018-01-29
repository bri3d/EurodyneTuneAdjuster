package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by brian.ledbetter on 1/28/18.
 */
class EcuIO() {
    data class EcuInfo(val swNumber: String, val swVersion: String, val vinNumber: String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(swNumber)
            parcel.writeString(swVersion)
            parcel.writeString(vinNumber)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EcuInfo> {
            override fun createFromParcel(parcel: Parcel): EcuInfo {
                return EcuInfo(parcel)
            }

            override fun newArray(size: Int): Array<EcuInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    fun getEcuInfo(io : ElmIO) : EcuInfo {
        var softwareVersion = ""
        var softwareNumber = ""
        var vinNumber = ""

        io.writeBytesBlocking("F1 88", {bytes ->
            softwareNumber = String(bytes!!)
        })
        io.writeBytesBlocking("F1 89", {bytes ->
            softwareVersion = String(bytes!!)
        })
        io.writeBytesBlocking("F1 90", {bytes ->
            vinNumber = String(bytes!!)
        })
        return EcuInfo(softwareNumber, softwareVersion, vinNumber)
    }

}