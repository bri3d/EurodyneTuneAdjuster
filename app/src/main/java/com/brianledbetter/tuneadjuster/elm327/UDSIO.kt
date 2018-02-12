package com.brianledbetter.tuneadjuster.elm327

import java.io.ByteArrayOutputStream
import java8.util.concurrent.CompletableFuture


/**
 * Created by brian.ledbetter on 2/11/18.
 */
class UDSIO(val io : ElmIO) {
    fun readLocalIdentifier(identifier: ByteArray) : CompletableFuture<ByteArray> {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x22)
        bytes.write(identifier)
        return io.writeBytesBlocking(bytes.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(3)?.toByteArray()
        }
    }

    fun writeLocalIdentifier(identifier: ByteArray, value: ByteArray) : CompletableFuture<ByteArray> {
        val bytes = ByteArrayOutputStream()
        bytes.write(0x2E)
        bytes.write(identifier)
        bytes.write(value)
        return io.writeBytesBlocking(bytes.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(3)?.toByteArray()
        }
    }
}