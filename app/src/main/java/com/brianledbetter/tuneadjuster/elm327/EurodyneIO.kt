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
        val minOctane = io.readLocalIdentifier(byteArrayOf(0xFD.toUByte(), 0x32.toUByte())).thenApply(::getFirstByteOrZero).join()
        val maxOctane = io.readLocalIdentifier(byteArrayOf(0xFD.toUByte(), 0x33.toUByte())).thenApply(::getFirstByteOrZero).join()
        val currentOctane = io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0xF9.toUByte())).thenApply(::getFirstByteOrZero).join()
        return OctaneInfo(minOctane, maxOctane, currentOctane)
    }

    fun getBoostInfo(io : UDSIO) : BoostInfo {
        val minBoost = io.readLocalIdentifier(byteArrayOf(0xFD.toUByte(), 0x30.toUByte())).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val maxBoost = io.readLocalIdentifier(byteArrayOf(0xFD.toUByte(), 0x31.toUByte())).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val currentBoost = io.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0xF8.toUByte())).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()

        return BoostInfo(minBoost, maxBoost, currentBoost)
    }

    fun setBoostInfo(io : UDSIO, boost : Int) {
        val writeBoostByte = calculateWriteBoost(boost)
        io.writeLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0xF8.toUByte()), byteArrayOf(writeBoostByte.toByte())).join()
    }

    fun setOctaneInfo(io : UDSIO, octane : Int) {
        io.writeLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0xF9.toUByte()), byteArrayOf(octane.toUByte())).join()

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