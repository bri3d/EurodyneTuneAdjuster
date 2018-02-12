package com.brianledbetter.tuneadjuster.elm327

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java8.util.concurrent.CompletableFuture

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class ElmIOReactor(private val inStream : InputStream) : Thread("ElmIOReactor") {
    companion object {
        const val TERMINAL = ">"
    }
    private val okHandlers = ListQueue<CompletableFuture<Boolean>>()
    private val messageHandlers = ListQueue<CompletableFuture<ByteArray>>()
    private val otherHandlers = ListQueue<CompletableFuture<String>>()
    private val whitespaceRegex = "\\s".toRegex()
    private val byteRegex = "([0-9A-F])+".toRegex()

    fun getNextOkFuture() : CompletableFuture<Boolean> {
        val okFuture = CompletableFuture<Boolean>()
        okHandlers.enqueue(okFuture)
        return okFuture
    }

    fun getNextMessageFuture() : CompletableFuture<ByteArray> {
        val nextMessageFuture = CompletableFuture<ByteArray>()
        messageHandlers.enqueue(nextMessageFuture)
        return nextMessageFuture
    }

    fun getNextOtherFuture() : CompletableFuture<String> {
        val nextOtherFuture = CompletableFuture<String>()
        otherHandlers.enqueue(nextOtherFuture)
        return nextOtherFuture
    }

    override fun run() {
        while(!Thread.interrupted()) {
            val currentLines = readUntilCharacter(TERMINAL).split("\r")
            val byteOutputStream = ByteArrayOutputStream()
            currentLines.forEach { currentLine ->
                when {
                    currentLine.startsWith("OK") -> okHandlers.dequeue()?.complete(true)
                    currentLine.replace(whitespaceRegex, "").matches(byteRegex) -> // Looks like hex
                        byteOutputStream.write(parseByteLine(currentLine))
                    else -> otherHandlers.dequeue()?.complete(currentLine)
                }
            }
            val resultBytes = byteOutputStream.toByteArray()
            if (resultBytes.isNotEmpty()) {
                messageHandlers.dequeue()?.complete(resultBytes)
            }
        }
    }

    private fun readUntilCharacter(character : String) : String {
        val buffer = StringBuilder()
        while(!Thread.interrupted()) {
            try {
                val byte = inStream.read()
                if (byte.toChar() == character.toCharArray()[0]) break
                buffer.append(byte.toChar())
            } catch (e : IOException) {
                return ""
            } catch (e : InterruptedException) {
                return ""
            }

        }
        return buffer.toString()
    }

    private fun parseByteLine(byteLine: String): ByteArray {
        val strippedByteLine = byteLine.replace(whitespaceRegex, "") // Remove whitespace
        if (!strippedByteLine.matches(byteRegex)) { // Check for hexish-ness
            return ByteArray(0)
        }
        return strippedByteLine.withIndex().groupBy { it.index / 2 }.values.map {it ->
           ((Character.digit(it.first().value, 16) shl 4) + Character.digit(it.last().value, 16)).toByte()
        }.toByteArray()
    }
}