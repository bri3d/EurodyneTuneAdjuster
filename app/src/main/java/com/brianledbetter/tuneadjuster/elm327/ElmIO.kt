package com.brianledbetter.tuneadjuster.elm327

import com.brianledbetter.tuneadjuster.HexUtil
import java.io.*
import java.nio.charset.Charset

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

    fun writeBytesBlocking(bytes : ByteArray, callback : (bytes : ByteArray?) -> Unit) {
        writeStringBlocking(HexUtil.bytesToHexString(bytes), callback)
    }

    fun writeStringBlocking(string : String, callback : (bytes : ByteArray?) -> Unit) {
        val messageFuture = ioReactor.getNextMessageFuture()
        try {
            writeString(string)
        } catch (e : IOException) {
            return
        }
        callback(messageFuture.join())
    }

    private fun writeString(string: String) {
        outputStream.write((string + "\r").toByteArray(Charset.forName("US-ASCII")))
    }
}