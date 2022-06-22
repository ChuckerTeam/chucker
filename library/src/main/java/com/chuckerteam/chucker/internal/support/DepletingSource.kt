package com.chuckerteam.chucker.internal.support

import okio.*
import java.io.IOException

internal class DepletingSource(delegate: Source) : ForwardingSource(delegate) {
    private var shouldDeplete = true

    override fun read(sink: Buffer, byteCount: Long) = try {
        val bytesRead = super.read(sink, byteCount)
        if (bytesRead == -1L) shouldDeplete = false
        bytesRead
    } catch (e: IOException) {
        shouldDeplete = false
        throw e
    }

    override fun close() {
        if (shouldDeplete) {
            try {
                delegate.buffer().readAll(blackholeSink())
            } catch (e: IOException) {
                IOException("An error occurred while depleting the source", e).printStackTrace()
            }
        }
        shouldDeplete = false

        super.close()
    }
}
