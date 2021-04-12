package com.chuckerteam.chucker.internal.support

import okio.Buffer
import okio.ForwardingSource
import okio.Source

internal class LimitingSource(
    delegate: Source,
    private val bytesCountThreshold: Long,
) : ForwardingSource(delegate) {
    private var bytesRead = 0L
    val isThresholdReached get() = bytesRead >= bytesCountThreshold

    override fun read(sink: Buffer, byteCount: Long) = if (!isThresholdReached) {
        super.read(sink, byteCount).also { bytesRead += it }
    } else {
        -1L
    }
}
