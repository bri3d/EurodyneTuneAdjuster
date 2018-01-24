package com.brianledbetter.tuneadjuster.elm327

import java.io.InputStream

/**
 * Created by brian.ledbetter on 1/20/18.
 */
class ElmIOReactor(private val inStream : InputStream) : Thread() {
    private val Terminal = ">"
    var okHandler : (() -> Boolean)? = null
    var messageHandler: ((ByteArray) -> Boolean)? = null
    var otherHandler: ((String) -> Boolean)? = null

    override fun run() {
        var exit = false
        while(!exit) {
            val currentLines = readUntilCharacter(Terminal).split("\r")
            // matchers would be sweet here
            currentLines.forEach { currentLine ->
                if (currentLine.startsWith("OK")) {
                    okHandler?.invoke()
                } else if (currentLine.replace("\\s".toRegex(), "").matches("([0-9A-F])+".toRegex())) {// Looks like hex
                    val bytes = parseByteLine(currentLine)
                    messageHandler?.invoke(bytes)
                } else {
                    otherHandler?.invoke(currentLine)
                }
            }
        }
    }

    private fun readUntilCharacter(character : String) : String {
        val buffer = StringBuilder()
        while(true) {
            while (inStream.available() == 0) {
                Thread.yield()
            }
            val byte = inStream.read()
            if (byte.toChar() == character.toCharArray()[0]) break
            buffer.append(byte.toChar())
        }
        return buffer.toString()
    }

    private fun parseByteLine(byteLine: String): ByteArray {
        var byteLine = byteLine
        byteLine = byteLine.replace("\\s".toRegex(), "") // Remove whitespace
        if (!byteLine.matches("([0-9A-F])+".toRegex())) { // Check for hexish-ness
            return ByteArray(0)
        }
        val len = byteLine.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(byteLine[i], 16) shl 4) + Character.digit(byteLine[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}