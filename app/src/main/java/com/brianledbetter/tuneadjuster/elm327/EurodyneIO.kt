package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable
import unsigned.Ubyte
import unsigned.toUInt
import unsigned.toUbyte


/**
 * Created by brian.ledbetter on 1/20/18.
 */
class EurodyneIO {
    data class OctaneInfo(val minimum: Int, val maximum: Int, val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt()) {
        }

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
                parcel.readInt()) {
        }

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

    fun getOctaneInfo(io : ElmIO) : OctaneInfo {
        var minOctane = 0
        var maxOctane = 0
        var currentOctane = 0
        var operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            minOctane = bytes[3].toUInt()
            operationReturned = true
            true
        }
        io.writeString("22 FD 32") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            maxOctane = bytes[3].toUInt()
            operationReturned = true
            true
        }
        io.writeString("22 FD 33") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            currentOctane = bytes[3].toUInt()
            operationReturned = true
            true
        }
        io.writeString("22 F1 F9") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        return OctaneInfo(minOctane, maxOctane, currentOctane)
    }

    fun getBoostInfo(io : ElmIO) : BoostInfo {
        var minBoost = 0
        var maxBoost = 0
        var currentBoost = 0
        var operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            minBoost = calculateBoost(bytes[3].toUbyte())
            operationReturned = true
            true
        }
        io.writeString("22 FD 30") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            maxBoost = calculateBoost(bytes[3].toUbyte())
            operationReturned = true
            true
        }
        io.writeString("22 FD 31") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        io.ioReactor.messageHandler = {bytes ->
            currentBoost = calculateBoost(bytes[3].toUbyte())
            operationReturned = true
            true
        }
        io.writeString("22 F1 F8") // ReadLocalIdentifier
        while (!operationReturned) Thread.yield()
        operationReturned = false
        return BoostInfo(minBoost, maxBoost, currentBoost)
    }

    fun setBoostInfo(io : ElmIO, boost : Int) {
        val writeBoostByte = calculateWriteBoost(boost)
        val boostByteString = String.format("%02x", writeBoostByte.toByte());
        var operationReturned = false
        io.ioReactor.messageHandler = {_ ->
            operationReturned = true
            true
        }
        io.writeString("2E F1 F8 " + boostByteString) // WriteLocalIdentifier
        while (!operationReturned) Thread.yield()
    }

    fun setOctaneInfo(io : ElmIO, octane : Int) {
        val octaneByteString = String.format("%02x", octane.toUbyte().toByte())
                var operationReturned = false
        io.ioReactor.messageHandler = {_ ->
            operationReturned = true
            true
        }
        io.writeString("2E F1 F9 " + octaneByteString) // WriteLocalIdentifier
        while (!operationReturned) Thread.yield()
    }

    fun calculateWriteBoost(psi: Int) : Ubyte {
        val offsetPsi = psi + 16
        val num = (offsetPsi.toDouble() / 0.014503773773).toInt()
        val num2 = (num.toDouble() * 0.047110065099374217).toInt()
        return num2.toUbyte()
    }

    fun calculateBoost(boost : Ubyte) : Int {
        val num = (boost.toDouble() / 0.047110065099374217).toInt()
        val num2 = (num.toDouble() * 0.014503773773).toInt()
        return num2 - 15
    }
}