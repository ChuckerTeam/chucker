package com.chuckerteam.chucker.internal.support

import java.io.File
import java.io.IOException
import okio.Buffer
import okio.Okio
import okio.Source
import okio.Timeout

/**
 * A source that acts as a tee operator - https://en.wikipedia.org/wiki/Tee_(command).
 *
 * It takes the input [upstream] and reads from it serving the bytes to the end consumer
 * like a regular [Source]. While bytes are read from the [upstream] the are also copied
 * to a [sideChannel] file. After the [upstream] is depleted or when a failure occurs
 * an appropriate [callback] method is called.
 *
 * Failure is considered any [IOException] during reading the bytes,
 * exceeding [readBytesLimit] length or not reading the whole upstream.
 */
internal class TeeSource(
    private val upstream: Source,
    private val sideChannel: File,
    private val callback: Callback,
    private val readBytesLimit: Long = Long.MAX_VALUE
) : Source {
    private val sideStream = Okio.buffer(Okio.sink(sideChannel))
    private var totalBytesRead = 0L
    private var isReadLimitExceeded = false
    private var isUpstreamExhausted = false
    private var isFailure = false

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = try {
            upstream.read(sink, byteCount)
        } catch (e: IOException) {
            callSideChannelFailure(e)
            throw e
        }

        if (bytesRead == -1L) {
            isUpstreamExhausted = true
            sideStream.close()
            return -1L
        }

        totalBytesRead += bytesRead
        if (!isReadLimitExceeded && (totalBytesRead <= readBytesLimit)) {
            val offset = sink.size() - bytesRead
            sink.copyTo(sideStream.buffer(), offset, bytesRead)
            sideStream.emitCompleteSegments()
            return bytesRead
        }
        if (!isReadLimitExceeded) {
            isReadLimitExceeded = true
            sideStream.close()
            callSideChannelFailure(IOException("Capacity of $readBytesLimit bytes exceeded"))
        }

        return bytesRead
    }

    override fun close() {
        sideStream.close()
        upstream.close()
        if (isUpstreamExhausted) {
            // Failure might have occurred due to exceeding limit.
            if (!isFailure) {
                callback.onSuccess(sideChannel)
            }
        } else {
            callSideChannelFailure(IOException("Upstream was not fully consumed"))
        }
    }

    override fun timeout(): Timeout = upstream.timeout()

    private fun callSideChannelFailure(exception: IOException) {
        if (!isFailure) {
            isFailure = true
            callback.onFailure(exception, sideChannel)
        }
    }

    interface Callback {
        /**
         * Called when the upstream was successfully copied to the [file].
         */
        fun onSuccess(file: File)

        /**
         * Called when there was an issue while copying bytes to the [file].
         *
         * It might occur due to one of the following reasons:
         * - an exception was thrown while reading bytes
         * - capacity limit was exceeded
         * - upstream was not fully consumed
         */
        fun onFailure(exception: IOException, file: File)
    }
}
