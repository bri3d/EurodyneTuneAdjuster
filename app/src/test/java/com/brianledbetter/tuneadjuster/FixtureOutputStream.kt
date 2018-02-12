package com.brianledbetter.tuneadjuster

import java.io.OutputStream

/**
 * Created by brian.ledbetter on 2/11/18.
 */
class FixtureOutputStream : OutputStream() {
    private val fixtureArray = mutableListOf<Byte>()
    var writeCallback : ((ByteArray) -> Unit)? = null
    override fun write(b: Int) {
        synchronized(fixtureArray, {
            fixtureArray.add(b.toByte())
        })
        writeCallback?.invoke(fixtureArrayAsBytes())
    }
    fun fixtureArrayAsBytes() : ByteArray {
        synchronized(fixtureArray, {
            return fixtureArray.toByteArray()
        })
    }
}