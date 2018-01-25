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
        var done = false
        reactor.okHandler = {
            done = true
            false
        }
        reactor.start()
        while(!done) Thread.yield()
        assertEquals(true, done)
        reactor.interrupt()
    }
    @Test
    fun readBytes() {
        val testInStream = ByteArrayInputStream("00 01 02 10>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        var done = false
        var testBytes : ByteArray? = null
        reactor.messageHandler = { bytes ->
            done = true
            testBytes = bytes
            false
        }
        reactor.start()
        while(!done) Thread.yield()
        assertEquals(0.toByte(), testBytes!![0])
        assertEquals(1.toByte(), testBytes!![1])
        assertEquals(2.toByte(), testBytes!![2])
        assertEquals(16.toByte(), testBytes!![3])
        reactor.interrupt()
    }
    @Test
    fun readOther() {
        val testInStream = ByteArrayInputStream("NO DATA>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        var done = false
        reactor.otherHandler = {
            done = true
            false
        }
        reactor.start()
        while(!done) Thread.yield()
        assertEquals(true, done)
        reactor.interrupt()
    }
    @Test
    fun readMultiple() {
        val testInStream = ByteArrayInputStream("00 01 02 10>\r03 02 01 00>".toByteArray())
        val reactor = ElmIOReactor(testInStream)
        var done = false
        var testBytes : ByteArray? = null
        reactor.messageHandler = { bytes ->
            done = true
            testBytes = bytes
            false
        }
        reactor.start()
        while(!done) Thread.yield()
        assertEquals(0.toByte(), testBytes!![0])
        assertEquals(1.toByte(), testBytes!![1])
        assertEquals(2.toByte(), testBytes!![2])
        assertEquals(16.toByte(), testBytes!![3])
        done = false
        while(!done) Thread.yield()
        assertEquals(3.toByte(), testBytes!![0])
        assertEquals(2.toByte(), testBytes!![1])
        assertEquals(1.toByte(), testBytes!![2])
        assertEquals(0.toByte(), testBytes!![3])
        reactor.interrupt()
    }
}
