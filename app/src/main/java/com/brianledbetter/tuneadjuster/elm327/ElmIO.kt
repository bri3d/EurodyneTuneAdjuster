package com.brianledbetter.tuneadjuster.elm327

import com.brianledbetter.tuneadjuster.util.HexUtil
import java.io.*
import java.nio.charset.Charset
import java8.util.concurrent.CompletableFuture

class ElmIO(private val inputStream : InputStream, private val outputStream: OutputStream) {
    private val ioReactor = ElmIOReactor(inputStream)

    fun start() {
        ioReactor.start()
        try {
            writeString("AT Z") // Reset
            Thread.sleep(500)
            waitForOther()
            writeString("AT E0") // Disable echo
            waitForOk()
            writeString("AT AL") // Allow long messages
            waitForOk()
            writeString("AT ST FF") // Timeout to maximum
            waitForOk()
            writeString("AT SP 0") // Autodetect protocol
            waitForOk()
            writeString("AT SH 7E0") // 7E0 communicates with ECU 1 in ISO15765-4 / UDS
            waitForOk()
        } catch (e : IOException) {
            stop()
        } catch (e : InterruptedException) {
            stop()
        }
    }

    fun stop() {
        ioReactor.interrupt()
        inputStream.close()
        outputStream.close()
    }

    fun waitForOther() {
        ioReactor.getNextOtherFuture().join()
    }

    fun waitForOk() {
       ioReactor.getNextOkFuture().join()
    }

    fun writeBytesBlocking(bytes : ByteArray) : CompletableFuture<ByteArray> {
        return writeStringBlocking(HexUtil.bytesToHexString(bytes))
    }

    fun writeStringBlocking(string : String) : CompletableFuture<ByteArray> {
        val messageFuture = ioReactor.getNextMessageFuture()
        val returnFuture = CompletableFuture<ByteArray>()
        try {
            writeString(string)
        } catch (e : IOException) {
            returnFuture.completeExceptionally(e)
        }
        returnFuture.complete(messageFuture.join())
        return returnFuture
    }

    private fun writeString(string: String) {
        outputStream.write((string + "\r").toByteArray(Charset.forName("US-ASCII")))
    }
}