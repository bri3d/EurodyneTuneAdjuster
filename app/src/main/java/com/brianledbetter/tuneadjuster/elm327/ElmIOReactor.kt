package com.brianledbetter.tuneadjuster.elm327

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.CompletableFuture

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class ElmIOReactor(private val inStream : InputStream) : Thread("ElmIOReactor") {
    private val Terminal = ">"
    var okHandlers = ListQueue<CompletableFuture<Boolean>>()
    var messageHandlers = ListQueue<CompletableFuture<ByteArray>>()
    var otherHandlers = ListQueue<CompletableFuture<String>>()
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
            val currentLines = readUntilCharacter(Terminal).split("\r")
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
            val byte = inStream.read()
            if (byte.toChar() == character.toCharArray()[0]) break
            buffer.append(byte.toChar())
        }
        return buffer.toString()
    }

    private fun parseByteLine(byteLine: String): ByteArray {
        val strippedByteLine = byteLine.replace(whitespaceRegex, "") // Remove whitespace
        if (!strippedByteLine.matches(byteRegex)) { // Check for hexish-ness
            return ByteArray(0)
        }
        val len = strippedByteLine.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len - 1) {
            data[i / 2] = ((Character.digit(strippedByteLine[i], 16) shl 4) + Character.digit(strippedByteLine[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}