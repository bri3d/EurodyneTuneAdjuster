package com.brianledbetter.tuneadjuster

import com.brianledbetter.tuneadjuster.elm327.ElmIO
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Created by brian.ledbetter on 1/29/18.
 */
class ElmIOTest {
    class FixtureOutputStream : OutputStream() {
        val fixtureArray = mutableListOf<Byte>()
        var writeCallback : ((Int) -> Unit)? = null
        override fun write(b: Int) {
            fixtureArray.add(b.toByte())
            writeCallback?.invoke(b)
        }
    }

    @Test
    fun writeBlockingTest() {
        val testInStream = PipedInputStream()
        var inStreamOut = PipedOutputStream(testInStream)
        val testOutStream = FixtureOutputStream()
        testOutStream.writeCallback = {_ ->
            inStreamOut.write("00 01 02 10\r >".toByteArray())
        }
        val elmIO = ElmIO(testInStream, testOutStream)
        var testBytes = byteArrayOf()
        val elmThread = Thread {
            elmIO.start()
        }
        elmThread.start()
        val readThread = Thread {
            elmIO.writeBytesBlocking("01 02", { bytes ->
                testBytes = bytes!!
            })
        }
        readThread.start()
        readThread.join()
        Assert.assertEquals(0.toByte(), testBytes[0])
        Assert.assertEquals(1.toByte(), testBytes[1])
        Assert.assertEquals(2.toByte(), testBytes[2])
        Assert.assertEquals(16.toByte(), testBytes[3])
        elmThread.interrupt()
    }

    @Test
    fun waitForOkTest() {
        val testInStream = PipedInputStream()
        var inStreamOut = PipedOutputStream(testInStream)
        val testOutStream = FixtureOutputStream()
        val elmIO = ElmIO(testInStream, testOutStream)
        val elmThread = Thread {
            elmIO.start()
        }
        elmThread.start()
        val readThread = Thread {
            elmIO.waitForOk()
        }
        readThread.start()
        inStreamOut.write("OK>\r".toByteArray())
        readThread.join()
    }

    @Test
    fun waitForOtherTest() {
        val testInStream = PipedInputStream()
        var inStreamOut = PipedOutputStream(testInStream)
        val testOutStream = FixtureOutputStream()
        val elmIO = ElmIO(testInStream, testOutStream)
        val elmThread = Thread {
            elmIO.start()
        }
        elmThread.start()
        val readThread = Thread {
            elmIO.waitForOther()
        }
        readThread.start()
        inStreamOut.write("NO DATA...>\r".toByteArray())
        readThread.join()
    }


}