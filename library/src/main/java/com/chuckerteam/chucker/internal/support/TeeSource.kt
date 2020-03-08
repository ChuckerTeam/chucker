package com.chuckerteam.chucker.internal.support

import java.io.File
import java.io.IOException
import okio.Buffer
import okio.ByteString
import okio.Okio
import okio.Source
import okio.Timeout

internal class TeeSource(
    private val upstream: Source,
    private val sideChannel: File,
    private val readBytesLimit: Long = Long.MAX_VALUE,
    private val onSideChannelReady: (File) -> Unit = {}
) : Source {
    init {
        Okio.buffer(Okio.sink(sideChannel)).use {
            it.write(PREFIX_OK_BYTES)
        }
    }

    private val sideStream = Okio.buffer(Okio.appendingSink(sideChannel))
    private var totalBytesRead = 0L
    private var reachedLimit = false

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = try {
            upstream.read(sink, byteCount)
        } catch (e: IOException) {
            writeFailureHeader()
            throw e
        }

        if (bytesRead == -1L) {
            sideStream.close()
            return -1L
        }

        totalBytesRead += bytesRead
        if (!reachedLimit && totalBytesRead <= readBytesLimit) {
            sink.copyTo(sideStream.buffer(), sink.size() - bytesRead, bytesRead)
            sideStream.emitCompleteSegments()
            return bytesRead
        }
        if (!reachedLimit) {
            reachedLimit = true
            writeFailureHeader()
        }

        return bytesRead
    }

    override fun close() {
        onSideChannelReady(sideChannel)
        sideStream.close()
        upstream.close()
    }

    override fun timeout(): Timeout {
        return upstream.timeout()
    }

    private fun writeFailureHeader() {
        sideStream.close()
        Okio.buffer(Okio.sink(sideChannel)).use {
            it.write(PREFIX_FAILURE_BYTES)
        }
    }

    companion object {
        const val PREFIX_OK = "Chucker ok"
        private val PREFIX_OK_BYTES = ByteString.encodeUtf8(PREFIX_OK + "\n")
        const val PREFIX_FAILURE = "Chucker failure"
        private val PREFIX_FAILURE_BYTES = ByteString.encodeUtf8(PREFIX_FAILURE + "\n")
    }
}
