package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.internal.support.getHostIp
import com.chuckerteam.chucker.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
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
                encoding = null,
            ),
        )
    }

    @Test
    fun `response is created correctly with body size`() {
        val response = HarTestUtils.createResponse("GET")

        assertThat(response?.bodySize).isEqualTo(1000)
    }

    @Test
    fun `host ip address from response successfully`() {
        val body =
            "{\"args\": {},\"origin\": \"192.168.1.1\", \"url\": \"https://httpbin.org/get\"}"
        val response = MockResponse().setBody(body).setResponseCode(200)
        val buffer = response.getBody()
        val hostIp = buffer?.getHostIp()
        assertThat(hostIp).isEqualTo("192.168.1.1")
    }
    @Test
    fun `host ip address from response doesn't exist`() {
        val body =
            "{\"args\": {}, \"url\": \"https://httpbin.org/get\"}"
        val response = MockResponse().setBody(body).setResponseCode(200)
        val buffer = response.getBody()
        val hostIp = buffer?.getHostIp()
        assertThat(hostIp).isEqualTo(null)
    }
}
