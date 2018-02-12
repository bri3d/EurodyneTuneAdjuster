package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable
import unsigned.*

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class EurodyneIO {
    data class OctaneInfo(val minimum: Int, val maximum: Int, val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(minimum)
            parcel.writeInt(maximum)
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<OctaneInfo> {
            override fun createFromParcel(parcel: Parcel): OctaneInfo {
                return OctaneInfo(parcel)
            }

            override fun newArray(size: Int): Array<OctaneInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class BoostInfo(val minimum: Int, val maximum: Int, val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(minimum)
            parcel.writeInt(maximum)
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<BoostInfo> {
            override fun createFromParcel(parcel: Parcel): BoostInfo {
                return BoostInfo(parcel)
            }

            override fun newArray(size: Int): Array<BoostInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    fun getFirstByteOrZero(inBytes : ByteArray?) : Int {
        return inBytes?.get(0)?.toUInt() ?: 0
    }

    fun getFirstUByteOrZero(inBytes : ByteArray?) : Ubyte {
        return inBytes?.get(0)?.toUbyte() ?: 0.toUbyte()
    }

    fun getOctaneInfo(io : UDSIO) : OctaneInfo {
        val minOctane = io.readLocalIdentifier(0xFD, 0x32).thenApply(::getFirstByteOrZero).join()
        val maxOctane = io.readLocalIdentifier(0xFD, 0x33).thenApply(::getFirstByteOrZero).join()
        val currentOctane = io.readLocalIdentifier(0xF1, 0xF9).thenApply(::getFirstByteOrZero).join()
        return OctaneInfo(minOctane, maxOctane, currentOctane)
    }

    fun getBoostInfo(io : UDSIO) : BoostInfo {
        val minBoost = io.readLocalIdentifier(0xFD, 0x30).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val maxBoost = io.readLocalIdentifier(0xFD, 0x31).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val currentBoost = io.readLocalIdentifier(0xF1, 0xF8).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()

        return BoostInfo(minBoost, maxBoost, currentBoost)
    }

    fun setBoostInfo(io : UDSIO, boost : Int) {
        val writeBoostByte = calculateWriteBoost(boost)
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF8), byteArrayOf(writeBoostByte.toByte())).join()
    }

    fun setOctaneInfo(io : UDSIO, octane : Int) {
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF9), byteArrayOf(octane.toUByte())).join()

    }

    private fun calculateWriteBoost(psi: Int) : Ubyte {
        val offsetPsi = psi + 16
        val num = (offsetPsi.toDouble() / 0.014503773773).toInt()
        val num2 = (num.toDouble() * 0.047110065099374217).toInt()
        return num2.toUbyte()
    }

    private fun calculateBoost(boost : Ubyte) : Int {
        val num = (boost.toDouble() / 0.047110065099374217).toInt()
        val num2 = (num.toDouble() * 0.014503773773).toInt()
        return num2 - 15
    }
}