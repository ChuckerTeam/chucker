package com.chuckerteam.chucker.api

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.Test

class ChuckerInterceptorTest {
    @get:Rule val server = MockWebServer()
    private val serverUrl = server.url("/") // Starts server implicitly
    private val mockContext = mockk<Context> {
        every { getString(any()) } returns ""
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(ChuckerInterceptor(mockContext))
        .build()

    @Test
    fun skipChuckerHeader_isNotAvailableForTheServerRequest() {
        server.enqueue(MockResponse().setBody("Hello, world!"))
        val request = Request.Builder().url(serverUrl)
            .addHeader(Chucker.SKIP_INTERCEPTOR_HEADER_NAME, "true")
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.request().header(Chucker.SKIP_INTERCEPTOR_HEADER_NAME)).isNull()
    }

    @Test
    fun doNotskipChuckerHeader_isNotAvailableForTheServerRequest() {
        server.enqueue(MockResponse().setBody("Hello, world!"))
        val request = Request.Builder().url(serverUrl)
            .addHeader(Chucker.SKIP_INTERCEPTOR_HEADER_NAME, "false")
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.request().header(Chucker.SKIP_INTERCEPTOR_HEADER_NAME)).isNull()
    }
}
