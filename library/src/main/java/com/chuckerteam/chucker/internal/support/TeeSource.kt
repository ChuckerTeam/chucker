package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.Okio
import okio.Source
import okio.Timeout
import java.io.File
import java.io.IOException

/**
 * A source that acts as a tee operator - https://en.wikipedia.org/wiki/Tee_(command).
 *
 * It takes the input [upstream] and reads from it serving the bytes to the end consumer
 * like a regular [Source]. While bytes are read from the [upstream] the are also copied
 * to a [sideChannel] file. After the [upstream] is depleted or when a failure occurs
 * an appropriate [callback] method is called.
 */
internal class TeeSource(
    private val upstream: Source,
    private val sideChannel: File,
    private val callback: Callback,
    private val readBytesLimit: Long = Long.MAX_VALUE
) : Source {
    private val sideStream = Okio.buffer(Okio.sink(sideChannel))
    private var totalBytesRead = 0L
    private var isFailure = false
    private var isClosed = false

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = try {
            upstream.read(sink, byteCount)
        } catch (e: IOException) {
            callSideChannelFailure(e)
            throw e
        }

        if (bytesRead == -1L) {
            sideStream.close()
            return -1L
        }

        val previousTotalByteRead = totalBytesRead
        totalBytesRead += bytesRead
        if (previousTotalByteRead >= readBytesLimit) {
            sideStream.close()
            return bytesRead
        }

        if (!isFailure) {
            copyBytesToFile(sink, bytesRead)
        }

        return bytesRead
    }

    private fun copyBytesToFile(sink: Buffer, bytesRead: Long) {
        val byteCountToCopy = if (totalBytesRead <= readBytesLimit) {
            bytesRead
        } else {
            bytesRead - (totalBytesRead - readBytesLimit)
        }
        val offset = sink.size() - bytesRead
        sink.copyTo(sideStream.buffer(), offset, byteCountToCopy)
        try {
            sideStream.emitCompleteSegments()
        } catch (e: IOException) {
            callSideChannelFailure(e)
        }
    }

    override fun close() {
        sideStream.close()
        upstream.close()
        if (!isClosed) {
            isClosed = true
            callback.onClosed(sideChannel)
        }
    }

    override fun timeout(): Timeout = upstream.timeout()

    private fun callSideChannelFailure(exception: IOException) {
        if (!isFailure) {
            isFailure = true
            sideStream.close()
            callback.onFailure(exception, sideChannel)
        }
    }

    interface Callback {
        /**
         * Called when the upstream was closed. All read bytes are copied to the [file].
         * This does not mean that the content of a [file] is valid. Only that the user
         * is done with the reading process.
         */
        fun onClosed(file: File)

        /**
         * Called when an exception was thrown while reading bytes from the upstream
         * or when writing to a side channel file fails. Any read bytes are available in a [file].
         */
        fun onFailure(exception: IOException, file: File)
    }
}
