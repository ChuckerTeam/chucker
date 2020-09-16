package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.ForwardingSource
import okio.Okio
import okio.Source
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
                Okio.buffer(delegate()).readAll(Okio.blackhole())
            } catch (e: IOException) {
                IOException("An error occurred while depleting the source", e).printStackTrace()
            }
        }
        shouldDeplete = false

        super.close()
    }
}
