package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ResponseTest {
    @Test
    fun `response is created correctly with status`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.status).isEqualTo(200)
    }

    @Test
    fun `response is created correctly with status text`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.statusText).isEqualTo("OK")
    }

    @Test
    fun `response is created correctly with http version`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.httpVersion).isEqualTo("HTTP")
    }

    @Test
    fun `response is created correctly with content`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.content).isEqualTo(
            Content(
                size = 1000,
                compression = null,
                mimeType = "application/json",
                text = """{"field": "value"}""",
                encoding = null
            )
        )
    }

    @Test
    fun `response is created correctly with body size`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.bodySize).isEqualTo(1000)
    }
}
