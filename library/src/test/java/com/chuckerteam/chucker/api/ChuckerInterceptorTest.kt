package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.getResourceFile
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.readByteStringBody
import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.ByteString
import okio.GzipSink
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import java.net.HttpURLConnection.HTTP_NO_CONTENT

class ChuckerInterceptorTest {
    enum class ClientFactory {
        APPLICATION {
            override fun create(interceptor: Interceptor): OkHttpClient {
                return OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
            }
        },
        NETWORK {
            override fun create(interceptor: Interceptor): OkHttpClient {
                return OkHttpClient.Builder()
                    .addNetworkInterceptor(interceptor)
                    .build()
            }
        };

        abstract fun create(interceptor: Interceptor): OkHttpClient
    }

    @get:Rule val server = MockWebServer()
    private val serverUrl = server.url("/") // Starts server implicitly
    private lateinit var chuckerInterceptor: ChuckerInterceptorDelegate

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        chuckerInterceptor = ChuckerInterceptorDelegate(TestFileFactory(tempDir))
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun imageResponse_isAvailableToChucker(factory: ClientFactory) {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type: image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val responseBody = ByteString.of(*chuckerInterceptor.expectTransaction().responseImageData!!)

        assertThat(responseBody).isEqualTo(expectedBody)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun imageResponse_isAvailableToTheEndConsumer(factory: ClientFactory) {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type: image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().body()!!.source().readByteString()

        assertThat(responseBody).isEqualTo(expectedBody)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedBody_isGunzippedForChucker(factory: ClientFactory) {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size()) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isResponseBodyPlainText).isTrue()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedBody_isGunzippedForTheEndConsumer(factory: ClientFactory) {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size()) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().body()!!.source().readByteString()

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedBody_withNoContent_isTransparentForChucker(factory: ClientFactory) {
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedBody_withNoContent_isTransparentForEndConsumer(factory: ClientFactory) {
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun regularBody_isAvailableForChucker(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isResponseBodyPlainText).isTrue()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun regularBody_isAvailableForTheEndConsumer(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun regularBody_withNoContent_isAvailableForChucker(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isResponseBodyPlainText).isTrue()
        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun regularBody_withNoContent_isAvailableForTheEndConsumer(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun payloadSize_dependsOnTheAmountOfDataDownloaded(factory: ClientFactory) {
        val segmentSize = 8_192L
        val body = Buffer().apply {
            repeat(10 * segmentSize.toInt()) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val source = client.newCall(request).execute().body()!!.source()
        source.use { it.readByteString(segmentSize) }

        val transaction = chuckerInterceptor.expectTransaction()
        // We cannot expect exact amount of data as there are no guarantees that client
        // will read from the source exact amount of data that we requested.
        //
        // It is only best effort attempt and if we download less than 8KiB reading will continue
        // in 8KiB batches until at least 8KiB is downloaded.
        assertThat(transaction.responsePayloadSize).isIn(Range.closed(segmentSize, 2 * segmentSize))
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun contentLength_isProperlyReadFromHeader_andNotFromAmountOfData(factory: ClientFactory) {
        val segmentSize = 8_192
        val body = Buffer().apply {
            repeat(10 * segmentSize) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val source = client.newCall(request).execute().body()!!.source()
        source.use { it.readByteString(segmentSize.toLong()) }

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseHeaders).contains(
            """
            |  {
            |    "name": "Content-Length",
            |    "value": "${body.size()}"
            |  }
            """.trimMargin()
        )
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseBody_isLimitedByInterceptorMaxContentLength(factory: ClientFactory, @TempDir tempDir: File) {
        val body = Buffer().apply {
            repeat(10_000) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            fileFactory = TestFileFactory(tempDir),
            maxContentLength = 1_000
        )
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody?.length).isEqualTo(1_000)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun payloadSize_isNotLimitedByInterceptorMaxContentLength(factory: ClientFactory, @TempDir tempDir: File) {
        val body = Buffer().apply {
            repeat(10_000) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            fileFactory = TestFileFactory(tempDir),
            maxContentLength = 1_000
        )
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseReplicationFailure_doesNotAffect_readingByConsumer(factory: ClientFactory, @TempDir tempDir: File) {
        assertThat(tempDir.deleteRecursively()).isTrue()

        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(TestFileFactory(tempDir))
        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().body()!!.source().readByteString()

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseReplicationFailure_doesNotInvalidate_ChuckerTransaction(factory: ClientFactory, @TempDir tempDir: File) {
        assertThat(tempDir.deleteRecursively()).isTrue()

        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(TestFileFactory(tempDir))
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody).isNull()
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size())
    }

    private class TestFileFactory(private val tempDir: File) : FileFactory {
        override fun create() = create("testFile")

        override fun create(filename: String) = File(tempDir, "testFile")
    }
}
