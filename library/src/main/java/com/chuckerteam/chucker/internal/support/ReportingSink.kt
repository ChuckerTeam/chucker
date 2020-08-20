package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.Okio
import okio.Sink
import okio.Timeout
import java.io.File
import java.io.IOException

/**
 * A sink that reports result of writing to it via [callback].
 *
 * It takes an input [downstreamFile] and writes to it bytes from a source. Amount of bytes
 * that is copied can be limited with [writeBytesLimit]. Results are reported back to a client
 * when this is is closed or when an exception occurs while creating a downstream sink or while
 * writing bytes.
 */
internal class ReportingSink(
    private val downstreamFile: File?,
    private val callback: Callback,
    private val writeBytesLimit: Long = Long.MAX_VALUE
) : Sink {
    private var totalByteCount = 0L
    private var isFailure = false
    private var isClosed = false
    private var downstream = try {
        if (downstreamFile != null) Okio.sink(downstreamFile) else null
    } catch (e: IOException) {
        callDownstreamFailure(IOException("Failed to use file $downstreamFile by Chucker", e))
        null
    }

    override fun write(source: Buffer, byteCount: Long) {
        val previousTotalByteCount = totalByteCount
        totalByteCount += byteCount
        if (previousTotalByteCount >= writeBytesLimit) return

        val bytesToWrite = if (previousTotalByteCount + byteCount <= writeBytesLimit) {
            byteCount
        } else {
            writeBytesLimit - previousTotalByteCount
        }

        if (bytesToWrite == 0L || isFailure) return

        downstream?.write(source, bytesToWrite)
    }

    override fun flush() {
        if (isFailure) return
        try {
            downstream?.flush()
        } catch (e: IOException) {
            callDownstreamFailure(e)
        }
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        safeCloseDownstream()
        callback.onClosed(downstreamFile, totalByteCount)
    }

    override fun timeout(): Timeout = downstream?.timeout() ?: Timeout.NONE

    private fun callDownstreamFailure(exception: IOException) {
        if (!isFailure) {
            isFailure = true
            safeCloseDownstream()
            callback.onFailure(downstreamFile, exception)
        }
    }

    private fun safeCloseDownstream() = try {
        downstream?.close()
    } catch (e: IOException) {
        callDownstreamFailure(e)
    }

    interface Callback {
        /**
         * Called when the sink is closed. All written bytes are copied to the [file].
         * This does not mean that the content of the [file] is valid. Only that the client
         * is done with the writing process. [writtenBytesCount] is the exact amount of data
         * that the client wrote even if the [file] is corrupted or does not exist.
         */
        fun onClosed(file: File?, writtenBytesCount: Long)

        /**
         * Called when an [exception] was thrown while processing data.
         * Any written bytes are available in a [file].
         */
        fun onFailure(file: File?, exception: IOException)
    }
}
