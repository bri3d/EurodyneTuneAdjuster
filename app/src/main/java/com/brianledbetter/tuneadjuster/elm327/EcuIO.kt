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

    fun returnToString(bytes : ByteArray?) : String {
        return String(bytes!!)
    }

    fun getEcuInfo(io : UDSIO) : EcuInfo {
        val softwareNumber = io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x88.toUByte())).thenApply(::returnToString).join()
        val softwareVersion = io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x89.toUByte())).thenApply(::returnToString).join()
        val vinNumber = io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x90.toUByte())).thenApply(::returnToString).join()

        return EcuInfo(softwareNumber, softwareVersion, vinNumber)
    }

}