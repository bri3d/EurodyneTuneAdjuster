package com.brianledbetter.tuneadjuster

import com.brianledbetter.tuneadjuster.elm327.ElmIOReactor
import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
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
    }
}
