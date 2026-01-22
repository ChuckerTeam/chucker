package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.google.common.truth.Truth.assertThat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Tests for timestamp fallback logic when Response.sentRequestAtMillis
 * and Response.receivedResponseAtMillis are zero (e.g., with Cronet/QUIC).
 */
internal class ChuckerInterceptorTimestampTest {
    @get:Rule
    val server = MockWebServer()

    private val serverUrl = server.url("/")

    @TempDir
    lateinit var tempDir: File

    /**
     * Interceptor that zeros out the timestamps to simulate Cronet/QUIC behavior
     */
    private class ZeroTimestampInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            // Create a new response with zero timestamps
            return response
                .newBuilder()
                .sentRequestAtMillis(0)
                .receivedResponseAtMillis(0)
                .build()
        }
    }

    @Test
    fun `GIVEN response with zero timestamps WHEN intercepted THEN fallback timestamps are used`() {
        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(chuckerInterceptor)
                .addNetworkInterceptor(ZeroTimestampInterceptor())
                .build()

        server.enqueue(MockResponse().setBody("test response"))
        val request = Request.Builder().url(serverUrl).build()

        val beforeRequest = System.currentTimeMillis()
        client.newCall(request).execute().use { response ->
            response.body?.string()
        }
        val afterResponse = System.currentTimeMillis()

        val transaction = chuckerInterceptor.expectTransaction()

        // Verify requestDate is set (from RequestProcessor, not overwritten by zero)
        assertThat(transaction.requestDate).isNotNull()
        assertThat(transaction.requestDate).isAtLeast(beforeRequest)
        assertThat(transaction.requestDate).isAtMost(afterResponse)

        // Verify responseDate is set using fallback (System.currentTimeMillis)
        assertThat(transaction.responseDate).isNotNull()
        assertThat(transaction.responseDate).isAtLeast(beforeRequest)
        assertThat(transaction.responseDate).isAtMost(afterResponse)

        // Verify duration is calculated from fallback timestamps
        assertThat(transaction.tookMs).isNotNull()
        assertThat(transaction.tookMs).isAtLeast(0L)

        // Verify timestamps are NOT epoch time (1970)
        assertThat(transaction.requestDateString).doesNotContain("1970")
        assertThat(transaction.responseDateString).doesNotContain("1970")

        chuckerInterceptor.expectNoTransactions()
    }

    @Test
    fun `GIVEN response with valid timestamps WHEN intercepted THEN OkHttp timestamps are used`() {
        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

        // Normal client without ZeroTimestampInterceptor
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(chuckerInterceptor)
                .build()

        server.enqueue(MockResponse().setBody("test response"))
        val request = Request.Builder().url(serverUrl).build()

        val beforeRequest = System.currentTimeMillis()
        client.newCall(request).execute().use { response ->
            response.body?.string()
        }
        val afterResponse = System.currentTimeMillis()

        val transaction = chuckerInterceptor.expectTransaction()

        // Verify timestamps are within expected range
        assertThat(transaction.requestDate).isNotNull()
        assertThat(transaction.requestDate).isAtLeast(beforeRequest)
        assertThat(transaction.requestDate).isAtMost(afterResponse)

        assertThat(transaction.responseDate).isNotNull()
        assertThat(transaction.responseDate).isAtLeast(beforeRequest)
        assertThat(transaction.responseDate).isAtMost(afterResponse)

        // Verify duration is positive
        assertThat(transaction.tookMs).isNotNull()
        assertThat(transaction.tookMs).isAtLeast(0L)

        chuckerInterceptor.expectNoTransactions()
    }

    @Test
    fun `GIVEN response with zero timestamps WHEN intercepted THEN duration is calculated correctly`() {
        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(chuckerInterceptor)
                .addNetworkInterceptor(ZeroTimestampInterceptor())
                .build()

        server.enqueue(MockResponse().setBody("test response"))
        val request = Request.Builder().url(serverUrl).build()

        client.newCall(request).execute().use { response ->
            response.body?.string()
        }

        val transaction = chuckerInterceptor.expectTransaction()

        // Verify duration is calculated (responseDate - requestDate)
        assertThat(transaction.tookMs).isNotNull()
        assertThat(transaction.tookMs).isEqualTo(
            transaction.responseDate!! - transaction.requestDate!!,
        )

        chuckerInterceptor.expectNoTransactions()
    }

    @Test
    fun `GIVEN response with partially zero timestamps WHEN intercepted THEN fallback is used for zero values`() {
        // Interceptor that only zeros receivedResponseAtMillis
        val partialZeroInterceptor =
            Interceptor { chain ->
                val response = chain.proceed(chain.request())
                response
                    .newBuilder()
                    .receivedResponseAtMillis(0)
                    .build()
            }

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(chuckerInterceptor)
                .addNetworkInterceptor(partialZeroInterceptor)
                .build()

        server.enqueue(MockResponse().setBody("test response"))
        val request = Request.Builder().url(serverUrl).build()

        val beforeRequest = System.currentTimeMillis()
        client.newCall(request).execute().use { response ->
            response.body?.string()
        }
        val afterResponse = System.currentTimeMillis()

        val transaction = chuckerInterceptor.expectTransaction()

        // requestDate should use OkHttp's sentRequestAtMillis (valid)
        assertThat(transaction.requestDate).isNotNull()
        assertThat(transaction.requestDate).isAtLeast(beforeRequest)

        // responseDate should use fallback (receivedResponseAtMillis is 0)
        assertThat(transaction.responseDate).isNotNull()
        assertThat(transaction.responseDate).isAtMost(afterResponse)

        // Duration should be calculated from mixed timestamps
        assertThat(transaction.tookMs).isNotNull()
        assertThat(transaction.tookMs).isAtLeast(0L)

        chuckerInterceptor.expectNoTransactions()
    }
}
