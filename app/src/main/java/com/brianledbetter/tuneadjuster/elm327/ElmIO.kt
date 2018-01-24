package com.brianledbetter.tuneadjuster.elm327

import java.io.*
import java.nio.charset.Charset

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class ElmIO(val inputStream : InputStream, val outputStream: OutputStream){
    val ioReactor = ElmIOReactor(inputStream)

    fun start(controllerAddress: String) {
        ioReactor.start()
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
        writeString("AT SH 7E0") // Talk to ECU via ye olde CAN headers
        waitForOk()
    }

    fun waitForOther() {
        var gotOther = false
        ioReactor.otherHandler = { otherString ->
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
        while (!gotOk) {
            Thread.yield()
        }
    }

    fun writeString(string: String) {
        outputStream.write((string + "\r").toByteArray(Charset.forName("US-ASCII")))
    }
}