package com.brianledbetter.tuneadjuster

object HexUtil {
    fun bytesToHexString(bytes: ByteArray): String {
        // ELM takes ASCII encoded bytes
        val sb = StringBuilder(bytes.size * 3 + 1)
        for (b in bytes) {
            sb.append(String.format("%02X ", b))
        }
        sb.append("\r")
        return sb.toString()
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