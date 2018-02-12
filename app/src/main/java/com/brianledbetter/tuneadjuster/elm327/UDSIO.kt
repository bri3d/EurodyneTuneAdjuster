package com.brianledbetter.tuneadjuster.elm327

import java.io.ByteArrayOutputStream
import java8.util.concurrent.CompletableFuture
import unsigned.toUByte


/**
 * Created by brian.ledbetter on 2/11/18.
 */
class UDSIO(private val io : ElmIO) {
    private fun intArrayToUnsigned(input : IntArray) : ByteArray {
        return input.map { i ->
            i.toUByte()
        }.toByteArray()
    }

    fun readLocalIdentifier(vararg identifier: Int) : CompletableFuture<ByteArray> {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x22)
        bytes.write(intArrayToUnsigned(identifier))
        return io.writeBytesBlocking(bytes.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(3)?.toByteArray()
        }
    }

    fun writeLocalIdentifier(identifier: IntArray, value: ByteArray) : CompletableFuture<ByteArray> {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x2E)
        bytes.write(intArrayToUnsigned(identifier))
        bytes.write(value)
        return io.writeBytesBlocking(bytes.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(3)?.toByteArray()
        }
    }
}