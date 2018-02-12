package com.brianledbetter.tuneadjuster

import com.brianledbetter.tuneadjuster.elm327.ElmIO
import com.brianledbetter.tuneadjuster.elm327.UDSIO
import org.junit.Assert
import org.junit.Test
import unsigned.toUByte
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Created by brian.ledbetter on 1/29/18.
 */
class UDSIOTest {
    @Test
    fun readLocalIdentifierTest() {
        val testInStream = PipedInputStream()
        val inStreamOut = PipedOutputStream(testInStream)
        val testOutStream = FixtureOutputStream()
        val elmIO = ElmIO(testInStream, testOutStream)
        var testBytes = byteArrayOf()
        val elmThread = Thread {
            elmIO.start()
        }
        elmThread.start()
        val udsIo = UDSIO(elmIO)
        udsIo.readLocalIdentifier(byteArrayOf(0xF1.toUByte(), 0x90.toUByte()), { bytes ->
            testBytes = bytes!!
        })
        inStreamOut.write(byteArrayOf(0, 0, 0, 0xFF.toUByte(), ">".toCharArray()[0].toByte()))
        Assert.assertTrue(String(testOutStream.fixtureArrayAsBytes()).contains("22 F1 90"))
        Assert.assertEquals(255.toUByte(), testBytes[0])
        elmThread.interrupt()
    }
}