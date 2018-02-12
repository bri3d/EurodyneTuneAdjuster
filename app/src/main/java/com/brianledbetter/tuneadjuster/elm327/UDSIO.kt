package com.brianledbetter.tuneadjuster.elm327

import unsigned.toUByte
import java.io.ByteArrayOutputStream


/**
 * Created by brian.ledbetter on 2/11/18.
 */
class UDSIO(val io : ElmIO) {
    fun readLocalIdentifier(identifier: ByteArray, callback : (bytes : ByteArray?) -> Unit) {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x22)
        bytes.write(identifier)
        io.writeBytesBlocking(bytes.toByteArray(), {readBytes ->
            callback(readBytes?.drop(3)?.toByteArray())
        })
    }

    fun writeLocalIdentifier(identifier: ByteArray, value: ByteArray, callback : (bytes : ByteArray?) -> Unit) {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x2E)
        bytes.write(identifier)
        bytes.write(value)
        io.writeBytesBlocking(bytes.toByteArray(), {readBytes ->
            callback(readBytes?.drop(3)?.toByteArray())
        })
    }
}