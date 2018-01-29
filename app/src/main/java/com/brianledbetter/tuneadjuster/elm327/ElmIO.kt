package com.brianledbetter.tuneadjuster.elm327

import java.io.*
import java.nio.charset.Charset

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class ElmIO(private val inputStream : InputStream, private val outputStream: OutputStream){
    val ioReactor = ElmIOReactor(inputStream)

    fun start() {
        ioReactor.start()
        try {
            writeString("AT Z") // Reset
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
        }
    }

    fun stop() {
        ioReactor.interrupt()
        inputStream.close()
        outputStream.close()
    }

    fun waitForOther() {
        var gotOther = false
        ioReactor.otherHandler = { _ ->
            gotOther = true
            true
        }
        while(!gotOther) Thread.yield()
    }

    fun waitForOk() {
        var gotOk = false
        ioReactor.okHandler = {
            gotOk = true
            true
        }
        while (!gotOk) Thread.yield()
    }

    fun writeString(string: String) {
        outputStream.write((string + "\r").toByteArray(Charset.forName("US-ASCII")))
    }

    fun writeBytesBlocking(string : String, callback : (bytes : ByteArray?) -> Unit) {
        var operationReturned = false
        var returnBytes : ByteArray? = null
        ioReactor.messageHandler = { bytes ->
            returnBytes = bytes
            operationReturned = true
            true
        }
        try {
            writeString(string)
        } catch (e : IOException) {
            return
        }
        while (!operationReturned) Thread.yield()
        callback(returnBytes)
    }
}