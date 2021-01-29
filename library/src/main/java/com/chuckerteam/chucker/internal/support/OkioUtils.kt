package com.chuckerteam.chucker.internal.support

import okio.Buffer
import java.io.EOFException
import kotlin.math.min

private const val MAX_PREFIX_SIZE = 64L
private const val CODE_POINT_SIZE = 16

/**
 * Returns true if the body in question probably contains human readable text. Uses a small sample
 * of code points to detect unicode control characters commonly used in binary file signatures.
 */
internal val Buffer.isProbablyPlainText
    get() = try {
        val prefix = Buffer()
        val byteCount = min(size, MAX_PREFIX_SIZE)
        copyTo(prefix, 0, byteCount)
        sequence { while (!prefix.exhausted()) yield(prefix.readUtf8CodePoint()) }
            .take(CODE_POINT_SIZE)
            .any { codePoint -> Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint) }
            .not()
    } catch (_: EOFException) {
        false // Truncated UTF-8 sequence
    }
