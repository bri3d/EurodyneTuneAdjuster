package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable
import unsigned.toUByte

/**
 * Created by brian.ledbetter on 1/28/18.
 */
class EcuIO {
    data class EcuInfo(val swNumber: String, val swVersion: String, val vinNumber: String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString())

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

    fun getEcuInfo(io : UDSIO) : EcuInfo {
        var softwareVersion = ""
        var softwareNumber = ""
        var vinNumber = ""

        io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x88.toUByte()), { bytes ->
            softwareNumber = String(bytes!!)
        })
        io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x89.toUByte()), { bytes ->
            softwareVersion = String(bytes!!)
        })
        io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x90.toUByte()), { bytes ->
            vinNumber = String(bytes!!)
        })
        return EcuInfo(softwareNumber, softwareVersion, vinNumber)
    }

}