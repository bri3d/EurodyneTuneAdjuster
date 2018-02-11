package com.brianledbetter.tuneadjuster

import com.brianledbetter.tuneadjuster.elm327.ElmIOReactor
import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayInputStream

class ElmIOReactorTest {
    @Test
    fun okayCalled() {
        val testInStream = ByteArrayInputStream("OK>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        val okFuture = reactor.getNextOkFuture()
        reactor.start()
        okFuture.join()
        reactor.interrupt()
    }
    @Test
    fun readBytes() {
        val testInStream = ByteArrayInputStream("00 01 02 10\r20\r>\r".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        val bytesFuture = reactor.getNextMessageFuture()
        reactor.start()
        val testBytes = bytesFuture.join()
        assertEquals(0.toByte(), testBytes[0])
        assertEquals(1.toByte(), testBytes[1])
        assertEquals(2.toByte(), testBytes[2])
        assertEquals(16.toByte(), testBytes[3])
        assertEquals(32.toByte(), testBytes[4])
        reactor.interrupt()
    }
    @Test
    fun readOther() {
        val testInStream = ByteArrayInputStream("NO DATA>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        val otherFuture = reactor.getNextOtherFuture()
        reactor.start()
        assertEquals("NO DATA", otherFuture.join())
        reactor.interrupt()
    }

    @Test
    fun readMultiple() {
        val testInStream = ByteArrayInputStream("00 01 02 10>\r03 02 01 00>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        var future = reactor.getNextMessageFuture()
        reactor.start()
        var testBytes = future.join()
        assertEquals(0.toByte(), testBytes!![0])
        assertEquals(1.toByte(), testBytes[1])
        assertEquals(2.toByte(), testBytes[2])
        assertEquals(16.toByte(), testBytes[3])
        future = reactor.getNextMessageFuture()
        testBytes = future.join()
        assertEquals(3.toByte(), testBytes[0])
        assertEquals(2.toByte(), testBytes[1])
        assertEquals(1.toByte(), testBytes[2])
        assertEquals(0.toByte(), testBytes[3])
        reactor.interrupt()
    }
}
