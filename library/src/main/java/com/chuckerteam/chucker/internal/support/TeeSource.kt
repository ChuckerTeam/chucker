package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.Sink
import okio.Source
import okio.Timeout
import java.io.IOException

/**
 * A source that acts as a tee operator - https://en.wikipedia.org/wiki/Tee_(command).
 *
 * It takes the input [upstream] and reads from it serving the bytes to the end consumer
 * like a regular [Source]. While bytes are read from the [upstream] the are also copied
 * to a [sideStream].
 */
internal class TeeSource(
    private val upstream: Source,
    private val sideStream: Sink
) : Source {
    private val tempBuffer = Buffer()
    private var isFailure = false

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = upstream.read(sink, byteCount)

        if (bytesRead == -1L) {
            safeCloseSideStream()
            return -1L
        }

        if (!isFailure) {
            copyBytesToSideStream(sink, bytesRead)
        }

        return bytesRead
    }

    private fun copyBytesToSideStream(sink: Buffer, bytesRead: Long) {
        val offset = sink.size - bytesRead
        sink.copyTo(tempBuffer, offset, bytesRead)
        try {
            sideStream.write(tempBuffer, bytesRead)
        } catch (_: IOException) {
            isFailure = true
            safeCloseSideStream()
        }
    }

    override fun close() {
        safeCloseSideStream()
        upstream.close()
    }

    private fun safeCloseSideStream() = try {
        sideStream.close()
    } catch (_: IOException) {
        isFailure = true
    }

    override fun timeout(): Timeout = upstream.timeout()
}
