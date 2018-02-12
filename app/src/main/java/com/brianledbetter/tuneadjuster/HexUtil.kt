package com.brianledbetter.tuneadjuster

object HexUtil {
    fun bytesToHexString(bytes: ByteArray): String {
        return bytes.joinToString(" ") { b -> String.format("%02X", b) }
    }

    fun bytesToInt(bytes: ByteArray): Int {
        var ret = 0
        var i = 0
        while (i < 4 && i < bytes.size) {
            ret = ret shl 8
            ret = ret or (bytes[i].toInt() and 0xFF)
            i++
        }
        return ret
    }
}