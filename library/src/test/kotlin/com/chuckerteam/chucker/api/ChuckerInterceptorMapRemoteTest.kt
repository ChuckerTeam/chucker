package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.util.ClientFactory
import com.chuckerteam.chucker.util.NoLoggerRule
import com.google.common.truth.Truth.assertThat
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorMapRemoteTest {
    private val originalServer = MockWebServer()
    private val targetServer = MockWebServer()

    private val originalUrl = originalServer.url("/original?page=1")
    private val targetUrl = targetServer.url("/mock?page=1")

    @TempDir
    lateinit var tempDir: File

    @AfterEach
    fun tearDown() {
        originalServer.shutdown()
        targetServer.shutdown()
    }

    private fun chucker(skipPaths: List<String> = emptyList()) =
        ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            skipPaths = skipPaths,
            mapRemoteTargetResolver = { url, _ -> if (url == originalUrl) targetUrl else null },
        )

    @Test
    fun `call matching a rule is sent to the target with its method, headers and body`() {
        targetServer.enqueue(MockResponse().setBody("mocked"))
        val chucker = chucker()
        val request =
            Request
                .Builder()
                .url(originalUrl)
                .header("X-Custom", "value")
                .post("payload".toRequestBody())
                .build()

        val body =
            ClientFactory.APPLICATION
                .create(chucker)
                .newCall(request)
                .execute()
                .use { it.body?.string() }

        assertThat(body).isEqualTo("mocked")
        assertThat(originalServer.requestCount).isEqualTo(0)
        val received = targetServer.takeRequest(1, TimeUnit.SECONDS)!!
        assertThat(received.path).isEqualTo("/mock?page=1")
        assertThat(received.method).isEqualTo("POST")
        assertThat(received.getHeader("X-Custom")).isEqualTo("value")
        assertThat(received.body.readUtf8()).isEqualTo("payload")
    }

    @Test
    fun `host header set by the app does not reach the target`() {
        targetServer.enqueue(MockResponse())
        val request =
            Request
                .Builder()
                .url(originalUrl)
                .header("Host", "original.example.com")
                .build()

        ClientFactory.APPLICATION
            .create(chucker())
            .newCall(request)
            .execute()
            .close()

        val received = targetServer.takeRequest(1, TimeUnit.SECONDS)!!
        assertThat(received.getHeader("Host")).isEqualTo("${targetUrl.host}:${targetUrl.port}")
    }

    @Test
    fun `transaction keeps the original url and records the target`() {
        targetServer.enqueue(MockResponse().setBody("mocked"))
        val chucker = chucker()

        ClientFactory.APPLICATION
            .create(chucker)
            .newCall(Request.Builder().url(originalUrl).build())
            .execute()
            .use { it.body?.string() }

        val transaction = chucker.expectTransaction()
        assertThat(transaction.url).isEqualTo(originalUrl.toString())
        assertThat(transaction.mappedUrl).isEqualTo(targetUrl.toString())
        assertThat(transaction.isMapRemoteIgnored).isFalse()
        assertThat(transaction.responseBody).isEqualTo("mocked")
    }

    @Test
    fun `failed mapped call records the target`() {
        targetServer.shutdown()
        val chucker = chucker()

        runCatching {
            ClientFactory.APPLICATION
                .create(chucker)
                .newCall(Request.Builder().url(originalUrl).build())
                .execute()
        }

        // The failure is reported to the collector, which keeps the transaction for inspection.
        val transaction = chucker.expectTransaction()
        assertThat(transaction.error).isNotNull()
        assertThat(transaction.mappedUrl).isEqualTo(targetUrl.toString())
    }

    @Test
    fun `call without a matching rule is left alone`() {
        originalServer.enqueue(MockResponse())
        val chucker = chucker()

        ClientFactory.APPLICATION
            .create(chucker)
            .newCall(Request.Builder().url(originalServer.url("/other")).build())
            .execute()
            .close()

        assertThat(originalServer.requestCount).isEqualTo(1)
        assertThat(targetServer.requestCount).isEqualTo(0)
        assertThat(chucker.expectTransaction().mappedUrl).isNull()
    }

    @Test
    fun `network interceptor ignores the rule instead of failing the call`() {
        originalServer.enqueue(MockResponse().setBody("real"))
        val chucker = chucker()

        val body =
            ClientFactory.NETWORK
                .create(chucker)
                .newCall(Request.Builder().url(originalUrl).build())
                .execute()
                .use { it.body?.string() }

        assertThat(body).isEqualTo("real")
        assertThat(targetServer.requestCount).isEqualTo(0)
        val transaction = chucker.expectTransaction()
        assertThat(transaction.mappedUrl).isNull()
        assertThat(transaction.isMapRemoteIgnored).isTrue()
    }

    @Test
    fun `network interceptor without a matching rule reports nothing`() {
        originalServer.enqueue(MockResponse())
        val chucker = chucker()

        ClientFactory.NETWORK
            .create(chucker)
            .newCall(Request.Builder().url(originalServer.url("/other")).build())
            .execute()
            .close()

        assertThat(chucker.expectTransaction().isMapRemoteIgnored).isFalse()
    }

    @Test
    fun `skipped call is not mapped`() {
        originalServer.enqueue(MockResponse())
        val chucker = chucker(skipPaths = listOf("/original"))

        ClientFactory.APPLICATION
            .create(chucker)
            .newCall(Request.Builder().url(originalUrl).build())
            .execute()
            .close()

        assertThat(originalServer.requestCount).isEqualTo(1)
        assertThat(targetServer.requestCount).isEqualTo(0)
        chucker.expectNoTransactions()
    }
}
