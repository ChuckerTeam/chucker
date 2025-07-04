package com.chuckerteam.chucker.internal.support

import android.text.SpannableStringBuilder
import androidx.core.text.getSpans
import com.chuckerteam.chucker.internal.support.SpanTextUtil.ChuckerForegroundColorSpan
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SpannableStringExtensionTest {
    @Test
    fun `chunks text correctly without any spans`() {
        val text = SpannableStringBuilder("ABCDEFGHIJ")

        val chunks = text.spannableChunked(3)

        assertEquals(4, chunks.size)
        assertEquals("ABC", chunks[0].toString())
        assertEquals("DEF", chunks[1].toString())
        assertEquals("GHI", chunks[2].toString())
        assertEquals("J", chunks[3].toString())

        chunks.forEach { chunk ->
            val allSpans = chunk.getSpans<Any>()
            assertTrue(allSpans.isEmpty())
        }
    }

    @Test
    fun `preserves span fully inside a chunk`() {
        val text = SpannableStringBuilder("ABCDEFGHIJ")
        val span = ChuckerForegroundColorSpan(0xFF00FF)
        // Span inside the first chunk
        text.setSpan(span, 1, 3, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        val chunks = text.spannableChunked(4)
        assertEquals(3, chunks.size)

        // Only chunk 0 should have the span
        val chunk0Spans = chunks[0].getSpans<ChuckerForegroundColorSpan>(0, 4)
        assertEquals(1, chunk0Spans.size)
        assertEquals(1, chunks[0].getSpanStart(chunk0Spans[0]))
        assertEquals(3, chunks[0].getSpanEnd(chunk0Spans[0]))

        val chunk1Spans = chunks[1].getSpans<ChuckerForegroundColorSpan>(0, 4)
        assertEquals(0, chunk1Spans.size)
    }

    @Test
    fun `splits span across chunks correctly`() {
        val text = SpannableStringBuilder("ABCDEFGHIJ")
        val span = ChuckerForegroundColorSpan(0x00FF00)
        // Span from C to H
        text.setSpan(span, 2, 7, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        val chunks = text.spannableChunked(3)
        assertEquals(4, chunks.size)

        // Chunk 0: "ABC", indices 0–3
        val chunk0Spans = chunks[0].getSpans<ChuckerForegroundColorSpan>(0, 3)
        assertEquals(1, chunk0Spans.size)
        assertEquals(2, chunks[0].getSpanStart(chunk0Spans[0]))
        assertEquals(3, chunks[0].getSpanEnd(chunk0Spans[0]))

        // Chunk 1: "DEF", indices 0–3
        val chunk1Spans = chunks[1].getSpans<ChuckerForegroundColorSpan>(0, 3)
        assertEquals(1, chunk1Spans.size)
        assertEquals(0, chunks[1].getSpanStart(chunk1Spans[0]))
        assertEquals(3, chunks[1].getSpanEnd(chunk1Spans[0]))

        // Chunk 2: "GHI", indices 0–3
        val chunk2Spans = chunks[2].getSpans<ChuckerForegroundColorSpan>(0, 3)
        assertEquals(1, chunk2Spans.size)
        assertEquals(0, chunks[2].getSpanStart(chunk2Spans[0]))
        assertEquals(1, chunks[2].getSpanEnd(chunk2Spans[0]))

        // Chunk 3: "J"
        val chunk3Spans = chunks[3].getSpans<ChuckerForegroundColorSpan>(0, 1)
        assertEquals(0, chunk3Spans.size)
    }

    @Test
    fun `handles span covering the whole text`() {
        val text = SpannableStringBuilder("ABCDEFGHIJ")
        val span = ChuckerForegroundColorSpan(0x123456)
        text.setSpan(span, 0, text.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        val chunks = text.spannableChunked(4)

        assertEquals(3, chunks.size)

        chunks.forEachIndexed { index, chunk ->
            val chunkSpans = chunk.getSpans<ChuckerForegroundColorSpan>(0, chunk.length)
            assertEquals(1, chunkSpans.size)
            assertEquals(0, chunk.getSpanStart(chunkSpans[0]))
            assertEquals(chunk.length, chunk.getSpanEnd(chunkSpans[0]))
        }
    }

    @Test
    fun `handles empty text`() {
        val text = SpannableStringBuilder("")
        val chunks = text.spannableChunked(4)

        assertTrue(chunks.isEmpty())
    }

    @Test
    fun `handles size larger than text length`() {
        val text = SpannableStringBuilder("Hello")
        val span = ChuckerForegroundColorSpan(0xABCDEF)
        text.setSpan(span, 0, 5, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        val chunks = text.spannableChunked(10)

        assertEquals(1, chunks.size)
        assertEquals("Hello", chunks[0].toString())

        val spans = chunks[0].getSpans<ChuckerForegroundColorSpan>(0, 5)
        assertEquals(1, spans.size)
        assertEquals(0, chunks[0].getSpanStart(spans[0]))
        assertEquals(5, chunks[0].getSpanEnd(spans[0]))
    }
}
