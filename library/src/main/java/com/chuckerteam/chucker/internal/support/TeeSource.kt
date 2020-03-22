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
 * Failure is considered any [IOException] during reading the bytes or exceeding [readBytesLimit] length.
 */
internal class TeeSource(
    private val upstream: Source,
    private val sideChannel: File,
    private val callback: Callback,
    private val readBytesLimit: Long = Long.MAX_VALUE
) : Source {
    private val sideStream = Okio.buffer(Okio.sink(sideChannel))
    private var totalBytesRead = 0L
    private var reachedLimit = false
    private var upstreamFailed = false

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

        totalBytesRead += bytesRead
        if (!reachedLimit && (totalBytesRead <= readBytesLimit)) {
            val offset = sink.size() - bytesRead
            sink.copyTo(sideStream.buffer(), offset, bytesRead)
            sideStream.emitCompleteSegments()
            return bytesRead
        }
        if (!reachedLimit) {
            reachedLimit = true
            callSideChannelFailure(IOException("Capacity of $readBytesLimit bytes exceeded"))
        }

        return bytesRead
    }

    override fun close() {
        sideStream.close()
        upstream.close()
        if (!upstreamFailed) {
            callback.onSuccess(sideChannel)
        }
    }

    override fun timeout(): Timeout = upstream.timeout()

    private fun callSideChannelFailure(exception: IOException) {
        if (!upstreamFailed) {
            upstreamFailed = true
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
         * It might occur due to an exception thrown while reading bytes or due to exceeding capacity limit.
         */
        fun onFailure(exception: IOException, file: File)
    }
}
