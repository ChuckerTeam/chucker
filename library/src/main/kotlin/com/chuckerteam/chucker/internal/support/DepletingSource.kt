package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.blackholeSink
import okio.buffer
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
                Logger.error("An error occurred while depleting the source", e)
            }
        }
        shouldDeplete = false

        super.close()
    }
}
