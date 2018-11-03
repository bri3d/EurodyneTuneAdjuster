package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable
import unsigned.*

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class EurodyneIO(private val io : UDSIO) {

    data class FeatureFlagInfo(val boostEnabled: Boolean, val octaneEnabled: Boolean, val e85Enabled: Boolean) : Parcelable {
        private fun Boolean.toInt() = if (this) 1 else 0

        constructor(parcel: Parcel) : this(
                parcel.readInt() > 0,
                parcel.readInt() > 0,
                parcel.readInt() > 0
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(boostEnabled.toInt())
            parcel.writeInt(octaneEnabled.toInt())
            parcel.writeInt(e85Enabled.toInt())
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FeatureFlagInfo> {
            override fun createFromParcel(parcel: Parcel): FeatureFlagInfo {
                return FeatureFlagInfo(parcel)
            }

            override fun newArray(size: Int): Array<FeatureFlagInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

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

    data class E85Info(val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<E85Info> {
            override fun createFromParcel(parcel: Parcel): E85Info {
                return E85Info(parcel)
            }

            override fun newArray(size: Int): Array<E85Info?> {
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

    private fun getFirstByteOrZero(inBytes : ByteArray?) : Int {
        return inBytes?.get(0)?.toUInt() ?: 0
    }

    private fun getFirstUByteOrZero(inBytes : ByteArray?) : Ubyte {
        return inBytes?.get(0)?.toUbyte() ?: 0.toUbyte()
    }

    fun getFeatureFlags() : FeatureFlagInfo {
        val featureFlags = io.readLocalIdentifier(0xFD, 0xFB).thenApply(::getFirstUByteOrZero).join()
        val boostEnabled = featureFlags and 2 > 0
        val octaneEnabled = featureFlags and 4 > 0
        val e85Enabled = featureFlags and 32 > 0
        return FeatureFlagInfo(boostEnabled, octaneEnabled, e85Enabled)
    }

    fun getOctaneInfo() : OctaneInfo {
        val minOctane = io.readLocalIdentifier(0xFD, 0x32).thenApply(::getFirstByteOrZero).join()
        val maxOctane = io.readLocalIdentifier(0xFD, 0x33).thenApply(::getFirstByteOrZero).join()
        val currentOctane = io.readLocalIdentifier(0xF1, 0xF9).thenApply(::getFirstByteOrZero).join()
        return OctaneInfo(minOctane, maxOctane, currentOctane)
    }

    fun getBoostInfo() : BoostInfo {
        val minBoost = io.readLocalIdentifier(0xFD, 0x30).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val maxBoost = io.readLocalIdentifier(0xFD, 0x31).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val currentBoost = io.readLocalIdentifier(0xF1, 0xF8).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()

        return BoostInfo(minBoost, maxBoost, currentBoost)
    }

    fun getE85Info() : E85Info {
        val currentE85 = io.readLocalIdentifier(0xF1, 0xFD).thenApply(::getFirstUByteOrZero).thenApply(::calculateE85).join();

        return E85Info(currentE85)
    }

    fun setBoostInfo(boost : Int) {
        val writeBoostByte = calculateWriteBoost(boost)
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF8), byteArrayOf(writeBoostByte.toByte())).join()
    }

    fun setOctaneInfo(octane : Int) {
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF9), byteArrayOf(octane.toUByte())).join()

    }

    fun setE85Info(e85 : Int) {
        val e85Byte = calculateWriteE85(e85)
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xFD), byteArrayOf(e85Byte.toByte())).join()
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

    private fun calculateWriteE85(e85 : Int) : Ubyte {
        return (e85.toDouble() * 1.28).toUbyte()
    }

    private fun calculateE85(e85 : Ubyte) : Int {
        val num = (e85.toDouble() / 1.28).toInt() + 1
        return num
    }
}