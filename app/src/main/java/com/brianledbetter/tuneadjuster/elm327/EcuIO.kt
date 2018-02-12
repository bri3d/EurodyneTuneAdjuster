package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by brian.ledbetter on 1/28/18.
 */
class EcuIO(private val udsIo : UDSIO) {
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

    fun getEcuInfo() : EcuInfo {
        val softwareNumber = udsIo.readLocalIdentifier(0xF1, 0x88).thenApply(::returnToString).join()
        val softwareVersion = udsIo.readLocalIdentifier(0xF1, 0x89).thenApply(::returnToString).join()
        val vinNumber = udsIo.readLocalIdentifier(0xF1, 0x90).thenApply(::returnToString).join()

        return EcuInfo(softwareNumber, softwareVersion, vinNumber)
    }

}